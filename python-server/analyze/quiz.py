from sqlalchemy.orm import Session

from util.open_ai import OpenAIUtil


def add_qa(record_id: int, text: str, lan: str):
    open_ai = OpenAIUtil()
    quiz_list = open_ai.get_qa(record_id=record_id, question=text, lan=lan)
    return quiz_list
