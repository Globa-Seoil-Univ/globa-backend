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
        self.dlq_topic = f"{topic}-dlq"  # DLQ 토픽 추가
        self.producer = KafkaProducer(
            bootstrap_servers=self.broker,
            key_serializer=lambda x: bytes(x, encoding='utf-8'),
            value_serializer=lambda x: json.dumps(x).encode('utf-8'),
            retries=0
        )

    def send_message(self, key, message, topic=None):
        """토픽을 지정할 수 있도록 수정"""
        target_topic = topic or self.topic  # 토픽이 지정되지 않으면 기본 토픽 사용

        attempt = 0
        max_retries = 3
        while attempt < max_retries:
            try:
                self.producer.send(
                    topic=target_topic,
                    key=key,
                    value=message,
                )
                self.logger.info(f"Message sent to broker : {key} -> {target_topic}")
                self.producer.flush()
                return True
            except Exception as e:
                attempt += 1
                self.logger.error(
                    f"[{attempt}/{max_retries}] Error sending message to {target_topic}: {message} cause {e.__str__()}"
                )
                time.sleep(1)

        # 3번 다 실패하면 failed 토픽 전송
        try:
            self.logger.error(f"Send to failed topic: {self.topic}")
            self.producer.send(
                topic=self.topic,
                key=key,
                value=message,
            )
            self.producer.flush()
        except Exception as e:
            self.logger.error(
                f"Error sending message to failed topic {self.topic}: {message} cause {e.__str__()}"
            )
        return False

    def send_to_dlq(self, record_id, user_id, failed_step, error_type, error_message, processing_status, retry_count):
        """DLQ로 메시지 전송"""
        from datetime import datetime

        dlq_message = {
            "recordId": record_id,
            "info": {
                "step": failed_step,
                "type": error_type,
                "message": str(error_message),
                "timestamp": datetime.utcnow().isoformat() + "Z",
                "retry_count": retry_count
            },
            "status": processing_status
        }

        # DLQ는 재시도 없이 바로 전송 (DLQ 자체가 실패하면 안 되므로)
        try:
            self.producer.send(
                topic=self.dlq_topic,
                key="dlq",
                value=dlq_message,
            )
            self.producer.flush()
            self.logger.error(f"Sent to DLQ: record_id={record_id}, failed_step={failed_step}")
            return True
        except Exception as e:
            self.logger.error(f"Critical Error: Failed to send to DLQ: {e}")
            return False

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

