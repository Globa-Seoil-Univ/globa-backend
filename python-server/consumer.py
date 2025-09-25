import os
import time
import concurrent.futures
from threading import Lock
from datetime import datetime

import boto3
from dotenv import load_dotenv

from analyze.keyword import add_keywords
from analyze.quiz import add_qa
from analyze.section import add_section
from analyze.summary import add_summary
from analyze.assign_text import assign_text
from analyze.stt import stt
from exception.NotFoundException import NotFoundException
from model.orm import AppUser, Record, FolderShare
from util.database import SessionMaker
from util.loadJSON import load_corrected_results_from_json
from util.log import Logger
from util.gpt import *

from util.AESUtil import AESUtil
from util.visibility_manager import VisibilityTimeoutManager

load_dotenv()

response_topic = os.environ.get('response-topic')
success_key = os.environ.get("success-key")
failed_key = os.environ.get('failed-key')
secret_key = os.environ.get('secret-key')
salt = os.environ.get('SALT')

region = os.environ.get('AWS_REGION')
queue_url = os.environ.get('SQS_QUEUE_URL')
spring_queue_url = os.environ.get('SQS_SPRING_QUEUE_URL')


class Consumer:
    broker = ""
    topic = ""
    group_id = ""
    logger = None
    executor = None
    # 동적인 쓰레드풀 생성을 위한 파라미터들
    executor_lock = None
    last_activity_time = 0
    thread_timeout = 60  # idle 쓰레드를 처리할 시간.

    def __init__(self, broker, topic, group_id):
        self.logger = Logger(name="consumer").logger

        ## SQS 컨버전 작업
        self.sqs = boto3.client('sqs', region_name=region, aws_access_key_id=os.environ.get('AWS_ACCESS_KEY_ID'), aws_secret_access_key=os.environ.get('AWS_SECRET_ACCESS_KEY'))
        # VisibilityTimeout 관리자 초기화
        self.visibility_manager = VisibilityTimeoutManager(
            sqs_client=self.sqs,
            queue_url=queue_url,
            logger=self.logger,
            visibility_timeout_seconds=int(os.environ.get("VISIBILITY_TIMEOUT_SECONDS", 1200)),
            extension_threshold_seconds=int(os.environ.get("EXTENSION_THRESHOLD_SECONDS", 300)),
            monitor_interval=int(os.environ.get("MONITOR_INTERVAL_SECONDS", 60))
        )

        self.broker = broker
        self.topic = topic
        self.group_id = group_id

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

        self.visibility_manager.start_monitoring()

        last_poll_time = datetime.now()
        poll_interval = 20

        try:
            while True:
                current_time = datetime.now()

                # 20초 검사하기
                time_since_last_poll = (current_time - last_poll_time).total_seconds()

                if time_since_last_poll >= poll_interval:
                    response = self.sqs.receive_message(
                        QueueUrl=queue_url,
                        MaxNumberOfMessages=10,
                        MessageAttributeNames=['All'],
                        WaitTimeSeconds=20,  # Long Polling으로 된거 같긴 한데, WHILE문 안에 있어서 패킷 잡아먹는 듯 함.
                        AttributeNames=['All'],
                    )

                    last_poll_time = datetime.now()

                    messages = response.get('Messages', [])

                    if messages:
                        self.logger.info(f"받은 메시지 수: {len(messages)}")
                        executor = self.get_executor()

                        for message in messages :
                            receipt_handle = message['ReceiptHandle']
                            self.visibility_manager.register_message(receipt_handle)
                            executor.submit(self.process_sqs_message, message)

                    self._cleanup_executor_if_needed()

                else:
                    time.sleep(1)
                    self._cleanup_executor_if_needed()

        except Exception as e:
            self.logger.error("Failed to process SQS: {0}".format(e))
            self.send_sqs_failure_message(record_id=0, user_id=0, message=str(e))
        finally:
            # 모니터링 하는거 정리
            self.visibility_manager.stop_monitoring()

            # 종료 시 쓰레드 풀이 있으면 정리
            if self.executor:
                self.executor.shutdown(wait=True)

    def _cleanup_executor_if_needed(self):
        current_time = time.time()
        with self.executor_lock:
            if (self.executor and
                    current_time - self.last_activity_time > self.thread_timeout and
                    len([f for f in self.executor._threads if f.is_alive()]) == 0):
                self.logger.info("장시간 작업 없음 :: 쓰레드 풀 정리")
                self.executor.shutdown(wait=False)
                self.executor = None

    def process_sqs_message(self, message):
        receipt_handle = message['ReceiptHandle']
        message_id = message.get('MessageId', 'unknown')
        group_id = message.get('Attributes', {}).get('MessageGroupId'),

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
            self.logger.info(f"SQS 메시지 처리 시작 - ID: {message_id}")

            # 메시지 처리 (기존 process_message 로직 사용)
            success = self.process_message_with_result(kafka_like_message, receipt_handle, message_id)

            if success:
                self.delete_sqs_message(receipt_handle, message_id)
                self.logger.info(f"메시지 처리 성공 및 삭제 완료 - ID: {message_id}")
            else:
                self.send_sqs_failure_message(message.value["recordId"], str(message.value["userId"]), f"메시지 처리 실패 - ID :{message_id}", receipt_handle=receipt_handle, message_id=message_id)

            self.sqs.delete_message(
                QueueUrl=queue_url,
                ReceiptHandle=receipt_handle
            )
            self.visibility_manager.unregister_message(receipt_handle)
        except Exception as e:
            self.logger.error(f"메시지 처리 실패: {e}")
            # KAFKA DLQ를 여기서 호출해야할듯?

    def process_message_with_result(self, message, receipt_handle, message_id):
        """
        기존 process_message를 수정하여 성공/실패 결과를 반환하도록 함
        """
        try:
            is_json = isinstance(message.value, dict)
            is_enough_data = "recordId" in message.value and "userId" in message.value and "lang" in message.value

            if not (is_json and is_enough_data):
                self.logger.error(f"유효하지 않은 메시지 형식")
                self.visibility_manager.unregister_message(receipt_handle)
                return False


            record_id = message.value["recordId"]
            aes_util = AESUtil(secret_key)
            user_id = aes_util.decrypt(str(message.value["userId"]))
            lan = message.value["lang"]

        except Exception as e:
            self.logger.error(f"❌ 메시지 파싱 실패: {e}")
            self.send_sqs_failure_message(record_id, str(message.value["userId"]), e.message, receipt_handle=receipt_handle, message_id=message_id)
            return False

        # 재시도 로직
        attempt = 0
        max_retries = 3
        last_error = None
        last_failed_step = None

        while attempt < max_retries:
            processing_status = {
                "stt": "NOT_STARTED",
                "addSection": "NOT_STARTED",
                "assignText": "NOT_STARTED",
                "addSummary": "NOT_STARTED",
                "addQa": "NOT_STARTED",
                "addKeywords": "NOT_STARTED"
            }

            try:
                with SessionMaker() as pre_session:
                    user = pre_session.query(AppUser).filter(AppUser.user_id == user_id).first()
                    if user is None:
                        self.logger.info(f"Not found user")
                        raise NotFoundException("No such user")

                    record = pre_session.query(Record).filter(Record.record_id == record_id).first()

                    if record is None:
                        raise NotFoundException("No such record")

                    if record.path is None:
                        raise NotFoundException("No such path")
                    folder_share = (pre_session.query(FolderShare).filter(FolderShare.folder_id == record.folder_id
                                                                      and FolderShare.owner_id == user.user_id)
                                    .first())
                    if folder_share is None:
                        raise NotFoundException("No such folder share")

                self.logger.info(f"🎯 오디오 분석 시작: {record_id}")

                # STT 처리
                processing_status["stt"] = "IN_PROGRESS"
                stt_results = stt(record.path, lan)
                # stt_results = load_corrected_results_from_json("./output/corrected_stt_20250914_145727.json")
                processing_status["stt"] = "SUCCESS"

                # 각 단계별 처리
                processing_status["addSection"] = "IN_PROGRESS"
                section_list = add_section(record_id=record_id, text=stt_results, lan=lan)
                processing_status["addSection"] = "SUCCESS"

                processing_status["assignText"] = "IN_PROGRESS"
                assign_text_list, assign_results = assign_text(record_id=record_id, text=stt_results, section_list=section_list)
                processing_status["assignText"] = "SUCCESS"

                processing_status["addSummary"] = "IN_PROGRESS"
                summaries, section_list = add_summary(record_id=record_id, assign_texts=assign_results,section_list=section_list, lan=lan)
                processing_status["addSummary"] = "SUCCESS"

                processing_status["addQa"] = "IN_PROGRESS"
                text = ''.join(result.text for result in stt_results)
                quiz = add_qa(record_id=record_id, text=text,lan=lan)
                processing_status["addQa"] = "SUCCESS"

                processing_status["addKeywords"] = "IN_PROGRESS"
                keywords = add_keywords(record_id=record_id, text=text, lan=lan)
                processing_status["addKeywords"] = "SUCCESS"

                with SessionMaker() as session:
                    try :
                        session.add_all(section_list)
                        session.flush()
                        if len(section_list) != len(assign_text_list) != len(summaries):
                            raise ValueError("section_list 길이와 assign_text_list, summaries 길이가 다릅니다.")
                        for section_obj, analysis_obj in zip(section_list, assign_text_list, ):
                            analysis_obj.section = section_obj  # section_id 할당

                        session.add_all(assign_text_list)
                        session.add_all(summaries)
                        session.add_all(quiz)
                        session.add_all(keywords)
                        session.commit()
                    except Exception as e:
                        session.rollback()

                self.send_sqs_success_message(record_id, str(message.value["userId"]))
                self.logger.info(f"오디오 분석 완료: {record_id}")
                return True

            except NotFoundException as e:
                self.logger.error(f"리소스 없음 - recordId: {record_id}, userId: {user_id}, 원인: {e.message}")
                self.send_sqs_failure_message(record_id, str(message.value["userId"]), e.message, receipt_handle=receipt_handle, message_id=message_id)
                return False

            except Exception as e:
                attempt += 1
                last_error = e

                # 실패한 단계 찾기
                for step, status in processing_status.items():
                    if status == "IN_PROGRESS":
                        last_failed_step = step
                        processing_status[step] = "FAILED"
                        break

                self.logger.error(f"❌ [{attempt}/{max_retries}] 분석 오류: {e}")

                if attempt < max_retries:
                    time.sleep(1)  # 재시도 전 대기
                else:
                    # 최종 실패 처리
                    self.send_sqs_failure_message(record_id, str(message.value["userId"]), f"분석 실패 (재시도 {max_retries}회)", receipt_handle=receipt_handle, message_id=message_id)

                    # DLQ로 전송
                    self.send_to_dlq(
                        record_id=record_id,
                        user_id=str(message.value["userId"]),
                        failed_step=last_failed_step or "unknown",
                        error_type=self.classify_error(last_error),
                        error_message=str(last_error),
                        processing_status=processing_status,
                        retry_count=max_retries
                    )
                    return False

        return False

    def send_sqs_success_message(self, record_id, user_id):
        try:
            message_body = {
                'recordId': record_id,
                'userId': user_id,
                'status': 'success',
            }

            message_group_id = f"response-{record_id}-{user_id}"
            deduplication_id = f"success-{record_id}-{int(time.time())}"

            send_params = {
                'QueueUrl': spring_queue_url,
                'MessageBody': json.dumps(message_body),
                'MessageAttributes': {
                    'key': {
                        'StringValue': success_key,
                        'DataType': 'String'
                    },
                    'status': {
                        'StringValue': 'success',
                        'DataType': 'String'
                    },
                    'message_type': {
                        'StringValue': 'response',
                        'DataType': 'String'
                    }
                }
            }

            # FIFO 큐인지 확인
            if queue_url.endswith('.fifo'):
                send_params['MessageGroupId'] = message_group_id
                send_params['MessageDeduplicationId'] = deduplication_id

            self.sqs.send_message(**send_params)
            self.logger.info(f"성공 메시지 전송 완료 - recordId: {record_id}")
        except Exception as e:
            self.logger.error(f"SQS 성공 : {e}")

    def send_sqs_failure_message(self, record_id, user_id, message, receipt_handle = None, message_id = None):
        try:
            message_body = {
                'recordId': record_id,
                'userId': user_id,
                'status': 'failed',
            }

            # FIFO 큐용 파라미터
            message_group_id = f"response-{record_id}-{user_id}"
            deduplication_id = f"failed-{record_id}-{int(time.time())}"

            send_params = {
                'QueueUrl': spring_queue_url,
                'MessageBody': json.dumps(message_body),
                'MessageAttributes': {
                    'key': {
                        'StringValue': failed_key,
                        'DataType': 'String'
                    },
                    'status': {
                        'StringValue': 'failed',
                        'DataType': 'String'
                    },
                    'message_type': {
                        'StringValue': 'response',
                        'DataType': 'String'
                    }
                }
            }

            # FIFO 큐인지 확인
            if queue_url.endswith('.fifo'):
                send_params['MessageGroupId'] = message_group_id
                send_params['MessageDeduplicationId'] = deduplication_id

            self.sqs.send_message(**send_params)
            if receipt_handle is not None and message_id is not None:
                self.delete_sqs_message(receipt_handle, message_id)
            self.logger.info(f"실패 메시지 전송 완료 - recordId: {record_id}")
        except Exception as e:
            self.logger.error(f"sQs 실패 : {e}")

    def delete_sqs_message(self, receipt_handle, message_id):
        """SQS 메시지 삭제"""
        try:
            self.sqs.delete_message(
                QueueUrl=queue_url,
                ReceiptHandle=receipt_handle
            )

            self.visibility_manager.unregister_message(receipt_handle)
            self.logger.info(f"🗑️ SQS 메시지 삭제 완료 - ID: {message_id}")

        except Exception as e:
            self.logger.error(f"❌ SQS 메시지 삭제 실패 - ID: {message_id}, Error: {e}")
            # 삭제 실패해도 처리는 계속 진행 (중복 처리 방지를 위해 visibility timeout 활용)

    def send_to_dlq(self, record_id, user_id, failed_step, error_type, error_message, processing_status, retry_count,
                    original_message=None):
        """DLQ(Dead Letter Queue)로 실패 메시지 전송"""
        try:
            dlq_message = {
                "recordId": record_id,
                "userId": user_id,
                "errorInfo": {
                    "step": failed_step,
                    "type": error_type,
                    "message": str(error_message),
                    "timestamp": datetime.utcnow().isoformat() + "Z",
                },
                "errorStatus": processing_status
            }

            # DLQ URL이 설정되어 있다면 DLQ로 전송
            dlq_url = os.environ.get('SQS_DLQ_URL')
            if dlq_url:
                message_group_id = f"dlq-{record_id}-{user_id}"
                deduplication_id = f"dlq-{record_id}-{int(time.time())}-{retry_count}"
                send_params = {
                    'QueueUrl': dlq_url,
                    'MessageBody': json.dumps(dlq_message),
                    'MessageAttributes': {
                        'failed_step': {
                            'StringValue': failed_step,
                            'DataType': 'String'
                        },
                        'retry_count': {
                            'StringValue': str(retry_count),
                            'DataType': 'Number'
                        }
                    }
                }

                # FIFO 큐인지 확인 (.fifo로 끝나는지)
                if dlq_url.endswith('.fifo'):
                    send_params['MessageGroupId'] = message_group_id
                    send_params['MessageDeduplicationId'] = deduplication_id

                self.sqs.send_message(**send_params)
                self.logger.info(f"📤 DLQ 전송 완료 - recordId: {record_id}")
            else:
                # DLQ가 없으면 로그만 남김
                self.logger.error(f"DLQ URL 미설정 - 실패 메시지: {dlq_message}")

        except Exception as e:
            self.logger.error(f"DLQ 전송 실패: {e}")

    def classify_error(self, error):
        """에러 타입 분류 - Exception 타입 기반"""
        import openai
        from sqlalchemy.exc import SQLAlchemyError
        from requests.exceptions import RequestException, Timeout, ConnectionError
        from json import JSONDecodeError

        if isinstance(error, openai.OpenAIError):
            return "OPENAI_API_ERROR"
        elif isinstance(error, (openai.RateLimitError, openai.APITimeoutError)):
            return "OPENAI_RATE_LIMIT_ERROR"
        elif isinstance(error, SQLAlchemyError):
            return "DATABASE_ERROR"
        elif isinstance(error, JSONDecodeError):
            return "JSON_PARSING_ERROR"
        elif isinstance(error, Timeout):
            return "TIMEOUT_ERROR"
        elif isinstance(error, ConnectionError):
            return "CONNECTION_ERROR"
        elif isinstance(error, RequestException):
            return "HTTP_REQUEST_ERROR"
        elif isinstance(error, ValueError):
            return "VALIDATION_ERROR"
        elif isinstance(error, KeyError):
            return "MISSING_KEY_ERROR"
        elif isinstance(error, FileNotFoundError):
            return "FILE_NOT_FOUND_ERROR"
        elif isinstance(error, PermissionError):
            return "PERMISSION_ERROR"
        else:
            error_str = str(error).lower()

            if any(keyword in error_str for keyword in ["openai", "api key", "quota", "billing"]):
                return "OPENAI_API_ERROR"
            elif any(keyword in error_str for keyword in ["database", "sql", "connection pool"]):
                return "DATABASE_ERROR"
            elif any(keyword in error_str for keyword in ["json", "parse", "decode"]):
                return "JSON_PARSING_ERROR"
            elif any(keyword in error_str for keyword in ["timeout", "timed out"]):
                return "TIMEOUT_ERROR"
            elif any(keyword in error_str for keyword in ["connection", "network", "unreachable"]):
                return "CONNECTION_ERROR"
            elif any(keyword in error_str for keyword in ["validation", "invalid", "required"]):
                return "VALIDATION_ERROR"
            else:
                return f"UNKNOWN_ERROR_{type(error).__name__}"