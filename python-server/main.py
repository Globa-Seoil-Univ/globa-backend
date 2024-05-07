import threading

from consumer import Consumer

if __name__ == '__main__':
    consumer = Consumer(broker='localhost:9094', group_id='globa_python_group', topic='audio-analyze')

    consumer_thread = threading.Thread(target=consumer.run)

    consumer_thread.start()
    consumer_thread.join()
