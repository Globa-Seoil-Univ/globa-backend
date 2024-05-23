from typing import List

from sqlalchemy.orm import Session

from util.open_ai import OpenAIUtil
from util.whisper import STTResults


def add_section(record_id: int, text: List[STTResults], session: Session):
    # text가 stt 객체 전체
    # session을 받아서 트랜잭션
    open_ai = OpenAIUtil()

    section_list = open_ai.get_section(record_id=record_id, stt=text)
    session.add_all(section_list)
    session.flush()
