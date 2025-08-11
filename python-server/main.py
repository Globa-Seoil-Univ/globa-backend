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


# from flask import Flask, request, jsonify
# app = Flask(__name__)

# from util.storage import FirebaseStorageManager
#
# storage_manager = FirebaseStorageManager()
#
# @app.route('/user/fcm', methods=['POST'])
# def user_fcm():
#     data = request.json
#     token = data.get("token")
#     title = data.get("title", "알림 제목 테스트뜨뜨ㅡ2")
#     body = data.get("body", "알림 내용 테스뜨1")
#
#     if not token:
#         return jsonify({"error": "FCM 토큰이 필요합니다."}), 400
#
#     try:
#         response = storage_manager.send_fcm_notification(token, title, body)
#         return jsonify({"success": True, "message_id": response}), 200
#     except Exception as e:
#         return jsonify({"error": str(e)}), 500