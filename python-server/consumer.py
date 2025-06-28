import os
import time
import concurrent.futures
from threading import Lock

from kafka import KafkaConsumer
from dotenv import load_dotenv

from analyze.keyword import add_keywords
from analyze.quiz import add_qa
from analyze.section import add_section
from analyze.summary import add_summary
from analyze.assign_text import assign_text
from analyze.stt import stt
from analyze.stt import stt2
from exception.NotFoundException import NotFoundException
from model.orm import AppUser, Record, FolderShare
from producer import Producer
from util.database import SessionMaker
from util.log import Logger
from util.gpt import *

load_dotenv()

response_topic = os.environ.get('response-topic')
success_key = os.environ.get("success-key")
failed_key = os.environ.get('failed-key')


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

                # 메시지가 있을 때만 executor 사용
                if messages:
                    executor = self.get_executor()

                    # 가져온 메시지 처리하는 부분,
                    for tp, msgs in messages.items():
                        for message in msgs:
                            executor.submit(self.process_message, message)
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
            user_id = message.value["userId"]
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
            while attempt < max_retries:
                with SessionMaker() as session:
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
                        # 지우면 안됨 임시 주석, 기존에는 stt를 호출했지만, 이젠 stt2를 호출해야함. 추후 메소드명 정리 필요
                        stt_results = stt2(record.path, lan)
                        # 테스트를 위한 stt_results 설정
                        # stt_results = stt2(str(record_id),"ko")

                        self.logger.info(f"result: {stt_results}")
                        add_section(record_id=record_id, text=stt_results, session=session)
                        assign_text(record_id=record_id, text=stt_results, session=session)
                        add_summary(record_id=record_id, session=session)

                        text = ''.join(result.text for result in stt_results)
                        add_qa(record_id=record_id, text=text, session=session)
                        add_keywords(record_id=record_id, text=text, session=session, lan=lan) # ja en ko

                        # 지우면 안됨 임시 주석, 커밋하는 부분. DB의 무결성 보증을 위해 잠시 주석했었음.
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
                        self.logger.error(f"[{attempt}/{max_retries}] Analyze Error : {e}")        
                        if attempt < max_retries:
                            time.sleep(1)

            # 재시도 모두 실패 시 failed 토픽 전송
            self.producer.send_message(key=failed_key,
                                       message={'recordId': record_id, 'userId': user_id,
                                                'message': f"Analyze failed after {max_retries}retries"})
        else:
            self.producer.send_message(key=failed_key,
                                       message={'recordId': record_id, 'userId': user_id,
                                                'message': f"Not valid message is_json: {is_json}, "
                                                           f"is_enough_data: {is_enough_data}, "
                                                           f"is_analyze: {is_analyze}"})
            self.logger.info(
                f"Not valid message is_json: {is_json}, is_enough_data: {is_enough_data}, is_analyze: {is_analyze}")
