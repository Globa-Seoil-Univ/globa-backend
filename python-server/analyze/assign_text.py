from typing import List

from model.orm import Section, Analysis
from util.open_ai import OpenAIUtil
from util.whisper import STTResults


def assign_text(record_id: int, text: List[STTResults],  session):
    open_ai = OpenAIUtil()
    sections = session.query(Section.section_id, Section.start_time, Section.end_time).filter(Section.record_id == record_id).all()

    assign_text_list = open_ai.assign_text(stt_origin=text, sections=sections)
    session.add_all(assign_text_list)
    session.flush()
