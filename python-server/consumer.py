import json
import time
from argparse import ArgumentParser
from configparser import ConfigParser
from confluent_kafka import Consumer, OFFSET_BEGINNING

from analyze.stt import stt

if __name__ == '__main__':
    # python consumer.py [--reset]
    # --reset을 할 시 reset_offset 함수로 들어가, 오래된 메세지부터 다시 처리하는 여부를 선택할 수 있다.
    # --reset을 사용해 메시지를 처음부터 다시 확인할 수 있게 한다.
    # 에러가 발생하여 메시지를 처리하지 못하고 다운되어도 kafka가 알아서 에러가 발생한 지점부터 시작한다.
    parser = ArgumentParser()
    parser.add_argument('--reset', action='store_true')
    args = parser.parse_args()

    config_parser = ConfigParser()
    config_file_path = 'kafka-config.ini'
    config_parser.read(config_file_path)
    config = dict(config_parser['default'])
    config.update(config_parser['consumer'])

    consumer = Consumer(config)

    def reset_offset(consumer, partitions):
        if args.reset:
            for p in partitions:
                p.offset = OFFSET_BEGINNING
            consumer.assign(partitions)


    topic = "audio-analyze"
    consumer.subscribe([topic], on_assign=reset_offset)

    try:
        while True:
            msg = consumer.poll(1.0)

            if msg is None:
                continue
            if msg.error():
                print(f'{msg.error()} on partition {msg.partition()} at offset {msg.offset()}')
                break

            key = msg.key().decode('utf-8')
            value = json.loads(msg.value().decode('utf-8'))
            is_json = isinstance(value, dict)
            is_enough_data = "progressId" in value and "recordId" in value and "path" in value

            if msg.topic() == 'audio-analyze' and key == 'analyze' and is_json and is_enough_data:
                # 1. STT 변환
                stt_results = stt(progressId=value["progressId"], recordId=value["recordId"], path=value["path"])

                # 2-1. 중요 키워드 추출
                # 2-2. 퀴즈 생성
                # 2-3. 섹션 나누기
                # 2-4. 섹션별 요약
                pass
            else:
                # 에러 토픽 전송
                print("Not enough messages")

    except KeyboardInterrupt:
        pass
    except Exception as e:
        # 에러 토픽 전송
        print("Unexpected error:", e.__str__())
        pass
    finally:
        # Leave group and commit final offsets
        consumer.close()
