import json
from typing import List

from kafka import KafkaConsumer

from analyze.keyword import add_keywords
from analyze.quiz import add_qa
from analyze.section import add_section
from analyze.summary import add_summary
from analyze.assign_text import assign_text
from analyze.stt import stt
from exception.NotFoundException import NotFoundException
from model.orm import Quiz
from producer import Producer
from util.database import get_session
from util.log import Logger
from util.gpt import *


class Consumer:
    broker = ""
    topic = ""
    group_id = ""
    consumer = None
    producer = None
    logger = None

    def __init__(self, broker, topic, group_id):
        self.logger = Logger(name="consumer").logger
        self.broker = broker
        self.topic = topic
        self.group_id = group_id
        self.consumer = KafkaConsumer(
            bootstrap_servers=self.broker,
            group_id=self.group_id,
            consumer_timeout_ms=1800000,
            auto_offset_reset="latest",
            enable_auto_commit=False,
            value_deserializer=lambda m: json.loads(m.decode('utf-8'))
        )
        self.producer = Producer(
            broker=self.broker,
            topic="response"
        )
        self.consumer.subscribe(self.topic)

    def run(self):
        self.logger.info("Starting consumer")

        try:
            for message in self.consumer:
                key = str(message.key, 'utf-8')

                is_json = isinstance(message.value, dict)
                is_enough_data = "recordId" in message.value and "userId" in message.value
                is_analyze = message.topic == self.topic and key == "analyze"

                record_id = message.value["recordId"]
                user_id = message.value["userId"]

                if is_json and is_enough_data and is_analyze:
                    session = get_session()

                    try:
                        # 여기서 record, user, folder_share 체크 다 하고 들어가기
                        stt_results = stt(record_id=record_id, user_id=user_id)

                        add_section(record_id=record_id, text=stt_results, session=session)
                        assign_text(record_id=record_id, text=stt_results, session=session)
                        add_summary(record_id=record_id, session=session)

                        text = ''.join(result.text for result in stt_results)
                        add_qa(record_id=record_id, text=text, session=session)
                        add_keywords(record_id=record_id, text=text, session=session)

                        session.commit()
                        self.producer.send_message(key='success', message={'recordId': record_id, 'userId': user_id})
                    except NotFoundException as e:
                        session.rollback()
                        self.logger.error("Notfound exception with recordId : %s, userId : %s cause message : %s"
                                          % (record_id, user_id, e.message))
                        self.producer.send_message(key='failed',
                                                   message={'recordId': record_id, 'userId': user_id,
                                                            'message': e.message})
                    except Exception as e:
                        session.rollback()
                        self.logger.error(f"Analyze Error : {e}")
                        self.producer.send_message(key='failed',
                                                   message={'recordId': record_id, 'userId': user_id,
                                                            'message': e.__str__()})
                else:
                    self.producer.send_message(key='failed',
                                               message={'recordId': record_id, 'userId': user_id,
                                                        'message': "Not valid message is_json: {0}, "
                                                                   "is_enough_data: {1}, "
                                                                   "is_analyze: {2}".format(
                                                                    is_json, is_enough_data, is_analyze)
                                                                    })
                    self.logger.info(
                        "Not valid message is_json: {0}, is_enough_data: {1}, is_analyze: {2}".format(is_json,
                                                                                                      is_enough_data,
                                                                                                      is_analyze))
        except Exception as e:
            self.logger.error("Exception: {0}".format(e))
            self.producer.send_message(key='failed', message={'recordId': 0, 'userId': 0, 'message': e.__str__()})
