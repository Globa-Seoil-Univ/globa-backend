import os
from typing import List

from sqlalchemy.orm import Session

from model.orm import Keyword
from util.keyword import KeywordUtil
from util.keyword_jp import KeywordUtilJP
from util.keyword_en import KeywordUtilEn


# def add_keywords(record_id: int, text: str, session: Session): # default korean
#     keyword_util = KeywordUtil()
#     keyword_result = keyword_util.get_keywords(text)
#     keywords: List[Keyword] = []
#
#     for keyword in keyword_result:
#         keywords.append(Keyword(record_id=record_id, word=keyword[0], importance=keyword[1]))
#
#     session.add_all(keywords)
def add_keywords(record_id: int, text: str, session: Session, lan: str):

    if lan == "jp":
        keyword_util = KeywordUtilJP() # 일본어 형태소 분석기
    elif lan == "en" :
        keyword_util = KeywordUtilEn()
    else :
        keyword_util = KeywordUtil() # default는 한국어로 설정
    keyword_result = keyword_util.get_keywords(text) 
    keywords: List[Keyword] = []

    for keyword in keyword_result:
        keywords.append(Keyword(record_id=record_id, word=keyword[0], importance=keyword[1]))

    # 지우면 안됨 임시 주석
    # session.add_all(keywords)
