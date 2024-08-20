import threading
import os

from consumer import Consumer
from dotenv import load_dotenv

load_dotenv()

broker_url = os.environ.get("consumer-broker-url")
group_id = os.environ.get('consumer-group-id')
topic = os.environ.get('consumer-topic')

if __name__ == '__main__':
    consumer = Consumer(broker=broker_url, group_id=group_id, topic=topic)

    consumer_thread = threading.Thread(target=consumer.run)

    consumer_thread.start()
    consumer_thread.join()
