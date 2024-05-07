from exception.NotFoundException import NotFoundException
from model.app_user import AppUser
from model.record import Record
from util.whisper import WhisperManager
from util.storage import FirebaseStorageManager
from util.log import Logger
from util.database import get_session

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
