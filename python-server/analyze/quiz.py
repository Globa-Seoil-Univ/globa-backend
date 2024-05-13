from util.database import get_session
from util.open_ai import OpenAIUtil


def add_qa(record_id: int, text: str):
    session = get_session()
    open_ai = OpenAIUtil()

    try:
        quiz_list = open_ai.get_qa(record_id=record_id, question=text)

        session.add_all(quiz_list)
        session.commit()
    except Exception as e:
        session.rollback()
        raise e
