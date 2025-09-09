from sqlalchemy.orm import Session

from model.orm import Section, Analysis
from util.open_ai import OpenAIUtil



def add_summary(record_id: int, assign_texts,section_list, lan: str):
    open_ai = OpenAIUtil()
    # datas = session.query(Section.section_id, Section.title, Analysis.content).join(Analysis, Section.section_id == Analysis.section_id).filter(Section.record_id == record_id).all()

    summary_list, section_list = open_ai.get_summary(datas=assign_texts,section_list=section_list, language=lan)
    return summary_list, section_list
    # session.add_all(summary_list)
