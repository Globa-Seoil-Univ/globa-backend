import json

from kafka import KafkaProducer

from util.log import Logger

import time


class Producer:
    broker = ""
    topic = ""
    producer = None
    logger = None
    def __init__(self, broker, topic):
        self.logger = Logger(name="producer").logger
        self.broker = broker
        self.topic = topic
        self.failed_topic = f"{topic}_failed"
        self.producer = KafkaProducer(
            bootstrap_servers=self.broker,
            key_serializer=lambda x: bytes(x, encoding='utf-8'),
            value_serializer=lambda x: json.dumps(x).encode('utf-8'),
            retries=0 # 기존 값 5, 내가 재시도 처리를 작성하기 위해 0으로 변경
        )

    def send_message(self, key, message):
        attempt = 0
        max_retries = 3
        while attempt < max_retries:
            try:
                self.producer.send(
                    topic=self.topic,
                    key=key,
                    value=message,
                )
                # 좀 더 추가적인 무언가가 작성이 가능해졌습니다.
                self.logger.info(f"Message sent to broker : {key}")
                self.producer.flush()
                return True  # 성공 시 종료
            except Exception as e:
                attempt += 1
                self.logger.error(
                    f"[{attempt}/{max_retries}] Error sending message to {self.topic}: {message} cause {e.__str__()}"
                )
                time.sleep(1)  # 1초 대기 후 재시도
        

        # 3번 다 실패하면 failed 토픽 전송! init에서 토픽 설정해줬음
        try:
            self.logger.error(f"Send to failed topic: {self.failed_topic}")
            self.producer.send(
                topic=self.failed_topic,
                key=key,
                value=message,
            )
            self.producer.flush()
        except Exception as e:
            self.logger.error(
                f"Error sending message to failed topic {self.failed_topic}: {message} cause {e.__str__()}"
            )
        return False