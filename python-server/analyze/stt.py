from util.whisper import WhisperManager
from util.storage import FirebaseStorageManager

storage_manager = FirebaseStorageManager()
whisper_manager = WhisperManager()


def stt(progressId: int, recordId: int, path: str):
    url = storage_manager.getDownloadUrl(path=path)
    stt_results = whisper_manager.stt(path=url)

    return stt_results
