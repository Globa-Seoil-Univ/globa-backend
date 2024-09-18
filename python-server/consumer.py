import os
import concurrent.futures

from kafka import KafkaConsumer
from dotenv import load_dotenv

from analyze.keyword import add_keywords
from analyze.quiz import add_qa
from analyze.section import add_section
from analyze.summary import add_summary
from analyze.assign_text import assign_text
from analyze.stt import stt
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
        self.executor = concurrent.futures.ThreadPoolExecutor(max_workers=5)  # 최대 5개의 스레드

    def run(self):
        self.logger.info("Starting consumer")

        try:
            for message in self.consumer:
                self.executor.submit(self.process_message, message)
        except Exception as e:
            self.logger.error("Failed to JSON : {0}".format(e))
            self.producer.send_message(key=failed_key, message={'recordId': 0, 'userId': 0, 'message': e.__str__()})
        finally:
            self.executor.shutdown(wait=True)

    def process_message(self, message):
        try:
            key = str(message.key, 'utf-8')
            is_json = isinstance(message.value, dict)
            is_enough_data = "recordId" in message.value and "userId" in message.value
            is_analyze = message.topic == self.topic and key == "analyze"

            record_id = message.value["recordId"]
            user_id = message.value["userId"]
        except Exception as e:
            self.logger.error("Exception: {0}".format(e))
            self.producer.send_message(key=failed_key, message={'recordId': 0, 'userId': 0, 'message': e.__str__()})
            return

        if is_json and is_enough_data and is_analyze:
            self.logger.info(f"In stt method recordId: {record_id} user_id: {user_id}")

            with SessionMaker() as session:
                try:
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
                    stt_results = stt(record.path)

                    add_section(record_id=record_id, text=stt_results, session=session)
                    assign_text(record_id=record_id, text=stt_results, session=session)
                    add_summary(record_id=record_id, session=session)

                    text = ''.join(result.text for result in stt_results)
                    add_qa(record_id=record_id, text=text, session=session)
                    add_keywords(record_id=record_id, text=text, session=session)

                    session.commit()
                    self.logger.info(f"Success analyzed audio : {record_id}")
                    self.producer.send_message(key=success_key, message={'recordId': record_id, 'userId': user_id})
                except NotFoundException as e:
                    session.rollback()
                    self.logger.error(
                        f"Not found exception with recordId : {record_id}, userId : {user_id} cause message : {e.message}")
                    self.producer.send_message(key=failed_key,
                                               message={'recordId': record_id, 'userId': user_id,
                                                        'message': e.message})
                except Exception as e:
                    session.rollback()
                    self.logger.error(f"Analyze Error : {e}")
                    self.producer.send_message(key=failed_key,
                                               message={'recordId': record_id, 'userId': user_id,
                                                        'message': str(e)})
        else:
            self.producer.send_message(key=failed_key,
                                       message={'recordId': record_id, 'userId': user_id,
                                                'message': f"Not valid message is_json: {is_json}, "
                                                           f"is_enough_data: {is_enough_data}, "
                                                           f"is_analyze: {is_analyze}"})
            self.logger.info(
                f"Not valid message is_json: {is_json}, is_enough_data: {is_enough_data}, is_analyze: {is_analyze}")
