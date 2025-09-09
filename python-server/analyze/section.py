from typing import List

from sqlalchemy.orm import Session
from sqlalchemy.testing.util import total_size

from util.open_ai import OpenAIUtil
from util.whisper import STTResults


def add_section(record_id: int, text: List[STTResults], lan: str):
    # text가 stt 객체 전체
    # session을 받아서 트랜잭션
    open_ai = OpenAIUtil()

    section_list = open_ai.get_section(record_id=record_id, stt=text, language=lan)


    return section_list
    # session.add_all(section_list)
    # total_size = len(section_list)
    #
    # batch_size = 4
    #
    # for i in range(0, total_size, batch_size):
    #     batch = section_list[i:i + batch_size]
    #
    #     try :
    #         session.add_all(batch)
    #         session.flush()
    #     except Exception as e:
    #         session.rollback()
    #         for j, section in enumerate(batch):
    #             try :
    #                 session.add(section)
    #                 session.flush()
    #             except Exception as e:
    #                 session.rollback()
    #
    # session.flush()
