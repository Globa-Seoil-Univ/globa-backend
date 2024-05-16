from typing import List

from exception.NotFoundException import NotFoundException
from model.orm import AppUser, Record

from util.whisper import WhisperManager, STTResults
from util.storage import FirebaseStorageManager
from util.log import Logger
from util.database import get_session

from re import sub

storage_manager = FirebaseStorageManager()
whisper_manager = WhisperManager()

session = get_session()
logger = Logger(name="stt").logger


def stt(record_id: int, user_id: int):
    logger.info(f"In stt method recordId: {record_id} user_id: {user_id}")

    user = session.query(AppUser).filter(AppUser.user_id == user_id).first()
    if user is None:
        raise NotFoundException("No such user")

    record = session.query(Record).filter(Record.record_id == record_id).first()
    if record is None:
        raise NotFoundException("No such record")


    if record.path is None:
        raise NotFoundException("No such path")

    url = storage_manager.getDownloadUrl(path=record.path)
    stt_results = whisper_manager.stt(path=url)

    return stt_results


def remove_noise_text(stt_results: List[STTResults]):
    removed_noise_results = []

    def limit_repeated_words(text: str, max_repeats: int) -> str:
        pattern = r'(\b\w+\b)(?:\s+\1){' + str(max_repeats - 1) + r',}'
        replace_pattern = r'\1' * max_repeats
        result = sub(pattern, replace_pattern, text)
        return result

    for result in stt_results:
        text = limit_repeated_words(result.text, max_repeats=4)
        removed_noise_results.append(text)
        print(text)
