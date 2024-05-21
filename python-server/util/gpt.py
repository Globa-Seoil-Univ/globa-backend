import json
import pymysql
from typing import List

import requests
import os
import openai


from util.whisper import STTResults


def read_stt_results(file_path: str) -> List[STTResults]:
    stt_results_list = []
    with open(file_path, 'r', encoding="UTF-8") as file:
        stt_data = json.load(file)

        for item in stt_data["result"]:
            text = item["text"]
            start = float(item["start"])
            end = float(item["end"])
            stt_result = STTResults(text, start, end)
            stt_results_list.append(stt_result)

    return stt_results_list


# 텍스트 전문을 이용해서, 주제별로 섹션 분리
