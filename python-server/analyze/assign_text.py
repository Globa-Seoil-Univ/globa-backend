from typing import List

from model.orm import Section, Analysis
from util.open_ai import OpenAIUtil
from util.whisper import STTResults


def assign_text(record_id: int, text: List[STTResults],  section_list: List[Section]):
    open_ai = OpenAIUtil()

    assign_text_list, assign_results = open_ai.assign_text(stt_origin=text, sections=section_list)

    return assign_text_list, assign_results
