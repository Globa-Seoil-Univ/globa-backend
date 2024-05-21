from typing import List

from sqlalchemy.orm import Session

from model.orm import Keyword
from util.database import get_session
from util.keyword import KeywordUtil


def add_keywords(record_id: int, text: str, session: Session):
    keyword_util = KeywordUtil()
    keyword_result = keyword_util.get_keywords(text)
    keywords: List[Keyword] = []
    session = get_session()

    for keyword in keyword_result:
        keywords.append(Keyword(record_id=record_id, word=keyword[0], importance=keyword[1]))

    session.add_all(keywords)
    session.commit()
