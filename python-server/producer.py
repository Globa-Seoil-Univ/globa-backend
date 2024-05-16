import json

from kafka import KafkaProducer

from util.log import Logger


class Producer:
    broker = ""
    topic = ""
    group_id = ""
    producer = None
    logger = None

    def __init__(self, broker, topic):
        self.logger = Logger(name="producer").logger
        self.broker = broker
        self.topic = topic
        self.producer = KafkaProducer(
            bootstrap_servers=self.broker,
            key_serializer=lambda x: bytes(x, encoding='utf-8'),
            value_serializer=lambda x: json.dumps(x).encode('utf-8'),
            retries=5
        )

    def send_message(self, key, message):
        try:
            self.producer.send(
                topic=self.topic,
                key=key,
                value=message,
            )
            self.producer.flush()
        except Exception as e:
            self.logger.error(f"Error sending message to {self.topic}: {message} cause {e.__str__()}")