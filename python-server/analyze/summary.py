from model.orm import Section, Script
from util.open_ai import OpenAIUtil



def add_summary(record_id: int,  session):
    open_ai = OpenAIUtil()
    datas = session.query(Section.section_id, Section.title, Script.text).join(Script, Section.section_id == Script.section_id).filter(Section.record_id == record_id).all()

    print(datas)
    print("ASdasdsad")
    summary_list = open_ai.get_summary(datas=datas)
    session.add_all(summary_list)
