import os
import firebase_admin

from dotenv import load_dotenv
from firebase_admin import credentials
from firebase_admin import storage
from firebase_admin import messaging

from exception.NotFoundException import NotFoundException


class FirebaseStorageManager:
    def __init__(self):
        load_dotenv()

        bucket_address = os.environ.get("firebase-bucket")
        cred = credentials.Certificate("./firebase.json")
        firebase_admin.initialize_app(cred, {
            'storageBucket': bucket_address,
        })
        self.bucket = storage.bucket()

    def getDownloadUrl(self, path: str) -> str:
        blob = self.bucket.blob(path)

        if not blob.exists():
            raise NotFoundException("Not found audio file")

        filename = path.split("/")[-1]
        download_dir = os.getcwd() + "/downloads/"

        # 디렉토리가 존재하지 않으면 생성합니다.
        if not os.path.exists(download_dir):
            os.makedirs(download_dir)

        local_file_path = download_dir + filename
        blob.download_to_filename(local_file_path)

        return local_file_path

    def send_fcm_notification(token, title, body):
        """
        token: FCM 디바이스 토큰임
        """
        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body
            ),
            token=token  # 푸시 알림을 받을 FCM 토큰
        )

        # 🔥 메시지 전송
        response = messaging.send(message)
        print(f"✅ FCM 메시지 전송 성공: {response}")