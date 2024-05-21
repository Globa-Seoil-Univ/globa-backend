from sqlalchemy.orm import Session

from model.orm import Section, Analysis
from util.open_ai import OpenAIUtil



def add_summary(record_id: int, session: Session):
    open_ai = OpenAIUtil()
    datas = session.query(Section.section_id, Section.title, Analysis.content).join(Analysis, Section.section_id == Analysis.section_id).filter(Section.record_id == record_id).all()

    summary_list = open_ai.get_summary(datas=datas)
    session.add_all(summary_list)
