import json
from typing import List

from kafka import KafkaConsumer

from exception.NotFoundException import NotFoundException
from util.log import Logger

from analyze.stt import stt, remove_noise_text
from util.open_ai import OpenAIUtil
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
        self.consumer.subscribe(self.topic)

    def run(self):
        self.logger.info("Starting consumer")

        for message in self.consumer:
            key = str(message.key, 'utf-8')

            print("%s:%d:%d: key=%s value=%s" % (
                message.topic, message.partition, message.offset, key, message.value))

            is_json = isinstance(message.value, dict)
            is_enough_data = "recordId" in message.value and "userId" in message.value
            is_analyze = message.topic == self.topic and key == "analyze"

            try:
                if is_json and is_enough_data and is_analyze:
                    # 여기서 record, user, folder_share 체크 다 하고 들어가기
                    # try:
                    #     stt_results = stt(record_id=message.value["recordId"], user_id=message.value["userId"])
                    # except NotFoundException as e:
                    #     # error topic 전송
                    #     self.logger.error("Notfound exception with recordId : %s, userId : %s cause message : %s" % (message.value["recordId"], message.value["userId"], e.message))
                    # except Exception as e:
                    #     self.logger.error("Exception with recordId : %s, userId : %s cause message : %s" % (message.value["recordId"], message.value["userId"], e.__str__()))

                    # 해당 코드는 stt 결과를 가져오기 위한 테스트 코드입니다.
                    # 실제  로직에서는 필요 없습니다.
                    # JSON 파일 경로 설정
                    file_path = 'downloads/stt.json'
                    stt_results = read_stt_results(file_path)
                    texts = ''.join(result.text for result in stt_results)

                    open_ai = OpenAIUtil()

                    try:
                        open_ai.add_qa(record_id=message.value["recordId"], question=texts)
                    except Exception as e:
                        # error topic 전송
                        self.logger.error(f"QA Error : {e}")

                    # 2-1. 섹션 나누기
                    # 2-2. 섹션별 요약
                    # 2-3. 중요 키워드 추출
                    # 2-4. 퀴즈 생성
                else:
                    self.logger.info(
                        "Not valid message is_json: {0}, is_enough_data: {1}, is_analyze: {2}".format(is_json,
                                                                                                      is_enough_data,
                                                                                                      is_analyze))
            except Exception as e:
                self.logger.error("Exception: {0}".format(e))
