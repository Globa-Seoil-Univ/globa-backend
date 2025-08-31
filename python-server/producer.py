import json

from util.log import Logger
import time


class Producer:
    broker = ""
    topic = ""
    logger = None

    def __init__(self, broker, topic):
        """ Deprecated ! """
        self.logger = Logger(name="producer").logger
        self.broker = broker
        self.topic = topic
        self.dlq_topic = f"{topic}-dlq"  # DLQ 토픽 추가

