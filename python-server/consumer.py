import json
from typing import List

from kafka import KafkaConsumer

from analyze.keyword import add_keywords
from analyze.quiz import add_qa
from analyze.stt import stt
from exception.NotFoundException import NotFoundException
from model.orm import Quiz
from producer import Producer
from util.database import get_session
from util.log import Logger

from util.whisper import STTResults


# STT json을 가져오기 위한 테스트 코드입니다.
# 실제 로직에서는 필요 없는 코드입니다.
def read_stt_results(file_path: str) -> List[STTResults]:
    stt_results_list = []
    with open(file_path, 'r', encoding="UTF-8") as file:
        stt_data = json.load(file)

        for item in stt_data["result"]:
            text = item["text"]
            start = float(item["start"])
            end = float(item["end"])
            stt_result = STTResults(text, start, end)
            stt_results_list.append(stt_result)

    return stt_results_list


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

                print("%s:%d:%d: key=%s value=%s" % (
                    message.topic, message.partition, message.offset, key, message.value))

                is_json = isinstance(message.value, dict)
                is_enough_data = "recordId" in message.value and "userId" in message.value
                is_analyze = message.topic == self.topic and key == "analyze"

                record_id = message.value["recordId"]
                user_id = message.value["userId"]


                if is_json and is_enough_data and is_analyze:
                    # 여기서 record, user, folder_share 체크 다 하고 들어가기

                    # try:
                    #     stt_results = stt(record_id=record_id, user_id=user_id)
                    # except NotFoundException as e:
                    #     # error topic 전송
                    #     self.logger.error("Notfound exception with recordId : %s, userId : %s cause message : %s"
                    #                       % (record_id, user_id, e.message))
                    #     self.producer.send_message(key='failed',
                    #                                message={'recordId': record_id, 'userId': user_id, 'message': e.message})
                    #     return
                    # except Exception as e:
                    #     self.logger.error("Exception with recordId : %s, userId : %s cause message : %s"
                    #                       % (record_id, user_id, e.__str__()))
                    #     self.producer.send_message(key='failed',
                    #                                message={'recordId': record_id, 'userId': user_id, 'message': e.__str__()})
                    #     return

                    # 해당 코드는 stt 결과를 가져오기 위한 테스트 코드입니다.
                    # 실제  로직에서는 필요 없습니다.
                    # JSON 파일 경로 설정
                    file_path = 'downloads/stt.json'
                    stt_results = read_stt_results(file_path)
                    text = ''.join(result.text for result in stt_results)

                    try:
                        add_qa(record_id=record_id, text=text)
                    except Exception as e:
                        self.logger.error(f"QA Error : {e}")
                        self.producer.send_message(key='failed',
                                                   message={'recordId': record_id, 'userId': user_id, 'message': e.__str__()})
                        return

                    try:
                        add_keywords(record_id=record_id, text=text)
                    except Exception as e:
                        self.logger.error(f"Keyword Error : {e}")
                        return

                    self.producer.send_message(key='success', message={
                        'recordId': record_id, 'userId': user_id
                    })
                else:
                    self.producer.send_message(key='failed',
                                               message={'recordId': record_id, 'userId': user_id,
                                                        'message': "Not valid message is_json: {0}, "
                                                                   "is_enough_data: {1}, "
                                                                   "is_analyze: {2}"
                                               .format(is_json,is_enough_data, is_analyze)})
                    self.logger.info(
                        "Not valid message is_json: {0}, is_enough_data: {1}, is_analyze: {2}".format(is_json,
                                                                                                      is_enough_data,
                                                                                                      is_analyze))
        except Exception as e:
            self.logger.error("Exception: {0}".format(e))
            self.producer.send_message(key='failed', message={'recordId': 0, 'userId': 0, 'message': e.__str__()})
