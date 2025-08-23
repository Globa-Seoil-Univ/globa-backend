import os
import time
import concurrent.futures
from threading import Lock

import boto3
from kafka import KafkaConsumer
from dotenv import load_dotenv

from analyze.keyword import add_keywords
from analyze.quiz import add_qa
from analyze.section import add_section
from analyze.summary import add_summary
from analyze.assign_text import assign_text
from analyze.stt import stt2
from exception.NotFoundException import NotFoundException
from model.orm import AppUser, Record, FolderShare
from producer import Producer
from util.AESUtil import AESUtil
from util.database import SessionMaker
from util.log import Logger
from util.gpt import *

from util.AESUtil import AESUtil
load_dotenv()

response_topic = os.environ.get('response-topic')
success_key = os.environ.get("success-key")
failed_key = os.environ.get('failed-key')
secret_key = os.environ.get('secret-key')

region = os.environ.get('AWS_REGION')
response_queue_url = os.environ.get('RESPONSE_SQS_QUEUE_URL')
queue_url = os.environ.get('SQS_QUEUE_URL')

class Consumer:
    broker = ""
    topic = ""
    group_id = ""
    consumer = None
    producer = None
    logger = None
    executor = None
    # 동적인 쓰레드풀 생성을 위한 파라미터들
    executor_lock = None
    last_activity_time = 0
    thread_timeout = 60  # idle 쓰레드를 처리할 시간.

    def __init__(self, broker, topic, group_id):
        ## SQS 컨버전 작업
        self.sqs = boto3.client('sqs', region_name=region)


        self.logger = Logger(name="consumer").logger
        self.broker = broker
        self.topic = topic
        self.group_id = group_id
        self.consumer = KafkaConsumer(
            bootstrap_servers=self.broker,
            group_id=self.group_id,
            auto_offset_reset="latest",
            enable_auto_commit=True,
            value_deserializer=lambda m: json.loads(m.decode('utf-8')),
        )
        self.producer = Producer(
            broker=self.broker,
            topic=response_topic
        )
        self.consumer.subscribe(self.topic)
        # 기존에는 5개의 쓰레드풀을 강제로 설정 했었음. 낭비되므로 일단 주석
        # self.executor = concurrent.futures.ThreadPoolExecutor(max_workers=5)  # 최대 5개의 스레드
        self.executor = None
        self.executor_lock = Lock()
        self.last_activity_time = time.time()

    def get_executor(self):
        """
            기존에 concurrent.futures.ThreadPoolExecutor(max_workers=5) 를 이용하여,
            n개의 쓰레드를 미리 생성 하였다면, 이 메소드를 이용하여 작업이 들어올 때, 동적으로 생성하게 유도
        """
        with self.executor_lock:
            current_time = time.time()

            # executor가 없거나 오래 비활성 상태였으면 새로 생성
            if self.executor is None or (current_time - self.last_activity_time > self.thread_timeout and
                                         isinstance(self.executor, concurrent.futures.ThreadPoolExecutor)):
                if self.executor:
                    self.logger.info("작업 없음: 기존 쓰레드 풀 정리 중...")
                    self.executor.shutdown(wait=False)

                self.logger.info("새 쓰레드 풀 생성 중...")
                self.executor = concurrent.futures.ThreadPoolExecutor(max_workers=5)

            # 활동 시간 갱신
            self.last_activity_time = current_time
            return self.executor

    def run(self):
        self.logger.info("Starting consumer")

        try:
            while True:
                # notion에 기록된 poll 메소드 이용 ( 1초 주기 )
                messages = self.consumer.poll(timeout_ms=1000)

                # SQS 작업 - 2
                response = self.sqs.receive_message(
                    QueueUrl=queue_url,
                    MaxNumberOfMessages=10, # 한 번에 받는 최대 수
                    MessageAttributeNames=['All'], # 속성명 종류
                    WaitTimeSeconds=20, # Long Polling 타임임
                    AttributeNames=['All'],
                    #VisibilityTimeout=60,

                )

                messages = response.get('Messages', [])

                # 메시지가 있을 때만 executor 사용
                if messages:
                    executor = self.get_executor()

                    # 가져온 메시지 처리하는 부분, - kafka
                    # for tp, msgs in messages.items():
                    #     for message in msgs:
                    #         executor.submit(self.process_message, message)

                    # sqs 식으로 변경 -3
                    for message in messages:
                        executor.submit(self.process_sqs_message, message)
                else:
                    # 메시지가 없을 때 쓰레드 풀 상태 확인
                    current_time = time.time()
                    with self.executor_lock:
                        # 마지막 활동 이후 일정 시간이 지나면 쓰레드 풀 정리
                        if (self.executor and
                                current_time - self.last_activity_time > self.thread_timeout and
                                len([f for f in self.executor._threads if f.is_alive()]) == 0):
                            self.logger.info("장시간 작업 없음 :: 쓰레드 풀 정리")
                            self.executor.shutdown(wait=False)
                            self.executor = None

                    # CPU 사용률 감소를 위한 짧은 대기
                    time.sleep(0.1)
        except Exception as e:
            self.logger.error("Failed to JSON : {0}".format(e))
            self.producer.send_message(key=failed_key, message={'recordId': 0, 'userId': 0, 'message': e.__str__()})
        finally:
            # 종료 시 쓰레드 풀이 있으면 정리
            if self.executor:
                self.executor.shutdown(wait=True)

    def process_message(self, message):
        try:
            key = str(message.key, 'utf-8')
            is_json = isinstance(message.value, dict)
            is_enough_data = "recordId" in message.value and "userId" in message.value
            is_analyze = message.topic == self.topic and key == "analyze"

            record_id = message.value["recordId"]
            aes_util = AESUtil(secret_key)

            user_id = aes_util.decrypt(str(message.value["userId"]))
            # 새로 추가, 유저로부터 language를 받아야함
            lan = message.value["language"]
        except Exception as e:
            self.logger.error("Exception: {0}".format(e))
            self.producer.send_message(key=failed_key, message={'recordId': 0, 'userId': 0, 'message': e.__str__()})
            return

        if is_json and is_enough_data and is_analyze:
            self.logger.info(f"In stt method recordId: {record_id} user_id: {user_id}")

            attempt = 0
            max_retries = 3
            last_error = None
            last_failed_step = None

            while attempt < max_retries:
                with SessionMaker() as session:
                    processing_status = {
                        "stt": "NOT_STARTED",  # STT 상태 추가
                        "ADD_SECTION": "NOT_STARTED",
                        "ASSIGN_TEXT": "NOT_STARTED",
                        "ADD_SUMMARY": "NOT_STARTED",
                        "ADD_QA": "NOT_STARTED",
                        "ADD_KEYWORDS": "NOT_STARTED"
                    }
                    current_failed_step = None
                    current_error = None
                    try:
                        # 지우면 안됨 임시 주석, 유저 유효성 검증
                        user = session.query(AppUser).filter(AppUser.user_id == user_id).first()
                        if user is None:
                            self.logger.info(f"Not found user")
                            raise NotFoundException("No such user")
                        record = session.query(Record).filter(Record.record_id == record_id).first()
                        if record is None:
                            raise NotFoundException("No such record")
                        if record.path is None:
                            raise NotFoundException("No such path")
                        folder_share = (session.query(FolderShare).filter(FolderShare.folder_id == record.folder_id
                                                                          and FolderShare.owner_id == user.user_id)
                                        .first())
                        if folder_share is None:
                            raise NotFoundException("No such folder share")

                        self.logger.info(f"Starting analyze audio: {record_id}")

                        try:
                            processing_status["stt"] = "IN_PROGRESS"
                            stt_results = stt2(record.path, lan)
                            processing_status["stt"] = "SUCCESS"
                            self.logger.info(f"STT result: {stt_results}")
                        except Exception as e:
                            processing_status["stt"] = "FAILED"
                            current_failed_step = "stt"
                            current_error = e
                            raise

                        # 각 단계별 처리 (에러 추적을 위해 수정)
                        try:
                            processing_status["ADD_SECTION"] = "IN_PROGRESS"
                            add_section(record_id=record_id, text=stt_results, session=session, lan=lan)
                            processing_status["ADD_SECTION"] = "SUCCESS"
                        except Exception as e:
                            processing_status["ADD_SECTION"] = "FAILED"
                            current_failed_step = "ADD_SECTION"
                            current_error = e
                            raise

                        try:
                            processing_status["ASSIGN_TEXT"] = "IN_PROGRESS"
                            assign_text(record_id=record_id, text=stt_results, session=session)
                            processing_status["ASSIGN_TEXT"] = "SUCCESS"
                        except Exception as e:
                            processing_status["ASSIGN_TEXT"] = "FAILED"
                            current_failed_step = "ASSIGN_TEXT"
                            current_error = e
                            raise

                        try:
                            processing_status["ADD_SUMMARY"] = "IN_PROGRESS"
                            add_summary(record_id=record_id, session=session, lan=lan)
                            processing_status["ADD_SUMMARY"] = "SUCCESS"
                        except Exception as e:
                            processing_status["ADD_SUMMARY"] = "FAILED"
                            current_failed_step = "ADD_SUMMARY"
                            current_error = e
                            raise

                        try:
                            processing_status["ADD_QA"] = "IN_PROGRESS"
                            text = ''.join(result.text for result in stt_results)
                            add_qa(record_id=record_id, text=text, session=session, lan=lan)
                            processing_status["ADD_QA"] = "SUCCESS"
                        except Exception as e:
                            processing_status["ADD_QA"] = "FAILED"
                            current_failed_step = "ADD_QA"
                            current_error = e
                            raise

                        try:
                            processing_status["ADD_KEYWORDS"] = "IN_PROGRESS"
                            add_keywords(record_id=record_id, text=text, session=session, lan=lan)
                            processing_status["ADD_KEYWORDS"] = "SUCCESS"
                        except Exception as e:
                            processing_status["ADD_KEYWORDS"] = "FAILED"
                            current_failed_step = "ADD_KEYWORDS"
                            current_error = e
                            raise

                        session.commit()

                        self.logger.info(f"Success analyzed audio : {record_id}")
                        self.producer.send_message(key=success_key, message={'recordId': record_id, 'userId': user_id})
                        return
                    except NotFoundException as e:
                        session.rollback()
                        self.logger.error(
                            f"Not found exception with recordId : {record_id}, userId : {user_id} cause message : {e.message}")
                        self.producer.send_message(key=failed_key,
                                                   message={'recordId': record_id, 'userId': user_id,
                                                            'message': e.message})
                        return
                    except Exception as e:
                        session.rollback()
                        attempt += 1
                        last_failed_step = current_failed_step
                        last_error = current_error
                        self.logger.error(f"[{attempt}/{max_retries}] Analyze Error : {e}")
                        if attempt < max_retries:
                            time.sleep(1)

            # 재시도 모두 실패 시 failed 토픽 전송
            self.producer.send_message(key=failed_key,
                                       message={'recordId': record_id, 'userId': user_id,
                                                'message': f"Analyze failed after {max_retries}retries"})
            self.producer.send_to_dlq(
                record_id=record_id,
                user_id=user_id,
                failed_step=last_failed_step or "unknown",
                error_type=self.producer.classify_error(last_error),
                error_message=str(last_error),
                processing_status=processing_status,
                retry_count=max_retries
            )

        else:
            self.producer.send_message(key=failed_key,
                                       message={'recordId': record_id, 'userId': user_id,
                                                'message': f"Not valid message is_json: {is_json}, "
                                                           f"is_enough_data: {is_enough_data}, "
                                                           f"is_analyze: {is_analyze}"})
            self.logger.info(
                f"Not valid message is_json: {is_json}, is_enough_data: {is_enough_data}, is_analyze: {is_analyze}")

    def process_sqs_message(self, message):
        receipt_handle = message['ReceiptHandle']

        try:
            body = json.loads(message['Body'])

            message_attributes = message.get('MessageAttributes', {})
            key = None
            if 'key' in message_attributes:
                key = message_attributes['key']['StringValue']

            kafka_like_message = type('Message', (), {
                'key': key.encode('utf-8') if key else b'analyze',
                'value': body,
                'topic': 'analyze'  # 고정값 또는 메시지 속성에서 가져오기
            })()

            self.process_message(kafka_like_message)

            self.sqs.delete_message(
                QueueUrl=queue_url,
                ReceiptHandle=receipt_handle
            )

        except Exception as e:
            self.logger.error(f"메시지 처리 실패: {e}")
            # KAFKA DLQ를 여기서 호출해야할듯?

    def send_sqs_success_message(self, record_id, user_id):
        try:
            message_body = {
                'recordId': record_id,
                'userId': user_id,
                'status': 'success'
            }

            self.sqs.send_message(
                QueueUrl=response_queue_url,
                MessageBody=json.dumps(message_body),
                MessageAttributes={
                    'key': {
                        'StringValue': success_key,
                        'DataType': 'String'
                    }
                }
            )
        except Exception as e:
            self.logger.error(f"SQS 성공 : {e}")

    def send_sqs_failure_message(self, record_id, user_id, message):
        try:
            message_body = {
                'recordId': record_id,
                'userId': user_id,
                'message': message,
                'status': 'failed'
            }

            self.sqs.send_message(
                QueueUrl=response_queue_url,
                MessageBody=json.dumps(message_body),
                MessageAttributes={
                    'key': {
                        'StringValue': failed_key,
                        'DataType': 'String'
                    }
                }
            )
        except Exception as e:
            self.logger.error(f"sQs 실패 : {e}")