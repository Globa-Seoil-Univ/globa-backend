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
def ask_chatgpt_lib(stt):
    conn = pymysql.connect(host='127.0.0.1', user ='root', password ='1234', db ='globa', charset ='utf8')

    cur = conn.cursor()

    openai.api_key = os.getenv("gpt-api-key")
    section_function_descriptions = [
        {
            "name": "get_section",
            "description": "내용을 요약한 주제별로 섹션을 나누어 텍스트를 담습니다..",
            "parameters": {
                "type": "object",
                "properties": {
                    "sections": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "subject": {
                                    "type": "string",
                                    "description": "해당 파트의 주제를 나타냅니다."
                                },
                                "start": {
                                    "type": "string",
                                    "description": "해당 파트의 시작 시간을 나타냅니다."
                                },
                                "end": {
                                    "type": "string",
                                    "description": "해당 파트의 종료 시간을 나타냅니다."
                                }
                            }
                        }
                    }
                },
                "required": ["subject", "start", "end"],
            },
        }
    ]

    start_index = 0

    responseContent = []

    while start_index < len(stt):  # 시작 인덱스가 stt 길이보다 작은 동안 계속 반복
        print(start_index)
        current_str = ""
        for i in range(start_index, len(stt)):  # start_index부터 시작
            current_str += stt[i].text + "*" + str(stt[i].start) + "," + str(stt[i].end) + "*" + "\n"

            if len(current_str) >= 10000:  # 현재 문자열 길이가 14000을 초과하면
                break  # 반복 중단

        completion = openai.chat.completions.create(
            model = "gpt-3.5-turbo",
            messages=[
                {
                    "role": "system",
                    "content": "너는 사용자가 제시하는 조건을 반드시 준수해서 사용자가 제시한 문장을 요약 분리해주는 분석 전문가야."
                },
                {
                    "role": "user",
                    "content": "다음의 텍스트를 분리 및 요약해서 json형태로 반환해줘. 단, 요약 조건은 다음과 같아.1. 시간의 순서대로 흘러가야해\n2.각 주제별로 영역을 나눠야해 이떄 주제는 너가 판단하기에 중요한 내용으로 요약해서 주제로 적어, 단, 너무 잘게 자르지는 말아줘. 쓸모없다고 생각되는 부분은 제거하고 생각해도 좋아. \n3. 제일 중요한 조건이야. 각 파트의 시작시간과 종료시간을 반드시 적어주어야만해. \n4.각 요약된 파트는 start시간과 end시간을 적어주어야해\n5.각 문장의 맨 뒤에 아스타리크(*)로 감쌓여 있는 숫자는 앞에서부터 시작시간, 종료시간이야. 시간을 이것으로 판단해\n6.각 영역은 다음의 형식을 꼭 지켜줘 ! 주제 - 시작시간, 종료시간\n7.파트의 종료 시간은 다음 파트의 시작시간 이전이어야만해. \n\n" + current_str
                }
            ],
            functions = section_function_descriptions,
            function_call = "auto",
            response_format = {"type": "json_object"},
            temperature = 0.5,
            top_p = 1
        )

        print(completion.choices[0].message.function_call.arguments)

        completion_json = json.loads(completion.choices[0].message.function_call.arguments)
        query = "INSERT INTO section (record_id, title, start_time, end_time) VALUES (%s, %s, %s, %s)"

        for section in completion_json['sections'] :
            if section :
                cur.execute(query, (3, section['subject'], section['start'], section['end']))

        responseContent.append(completion.choices[0].message.function_call.arguments)

        start_index = i + 1  # 다음 시작 인덱스 업데이트
    print(responseContent)
    conn.commit()
    conn.close()
    return responseContent  # API 응답을 JSON 형태로 반환

def assign_text(stt, recordId):
    conn = pymysql.connect(host='localhost', user='root',
                           password='1234', db='globa', charset='utf8')

    cursor = conn.cursor()

    sql = "SELECT section_id, start_time, end_time FROM section WHERE record_id = %s"
    insertQuery = "INSERT INTO script (section_id, text) VALUES (%s, %s)"

    cursor.execute(sql, recordId)
    sections = cursor.fetchall()
    start_index = 0
    print(sections)
    for section in sections:
        print(section, "\n")
        current_str = ""  # 현재 섹션의 텍스트를 저장할 변수
        if section :
            for i in range(start_index, len(stt)):
                if stt[i].start <= section[2]:
                    current_str += stt[i].text  # 시간 범위 내의 텍스트 추가
                elif current_str:  # 범위를 벗어나는 경우이며, current_str에 이미 텍스트가 있는 경우
                    cursor.execute(insertQuery, (section[0], current_str))
                    start_index = i
                    current_str = ""
                    break
            if current_str:  # 마지막으로 범위 내 텍스트가 남아 있는 경우 출력
                cursor.execute(insertQuery, (section[0], current_str))
    conn.commit()
    conn.close()

def summary_section(recordId) :
    connMysql = pymysql.connect(host='localhost', user='root',
                           password='1234', db='globa', charset='utf8')
    cursorMySQL = connMysql.cursor()

    openai.api_key = os.getenv("gpt-api-key")
    summary_function_descriptions = [
        {
            "name": "get_section",
            "description": "본문을 주제에 맞게 요약한 텍스트를 담습니다..",
            "parameters": {
                "type": "object",
                "properties": {
                    "summaries": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "text": {
                                    "type": "string",
                                    "description": "텍스트를 주제에 적합한 내용으로 요약하여 담습니다."
                                }
                            }
                        }
                    }
                },
                "required": ["text"],
            },
        }
    ]



    sectionSql = "SELECT section_id, title FROM section WHERE record_id = %s"
    scriptSql = "SELECT text FROM script WHERE section_id = %s"
    summarySql = "INSERT INTO summary (section_id, content) VALUES (%s, %s)"

    cursorMySQL.execute(sectionSql, recordId)
    sections = cursorMySQL.fetchall()
    for section in sections :
        if section :
            cursorMySQL.execute(scriptSql, (section[0]))
            scriptItem = cursorMySQL.fetchone()
            if scriptItem:
                completion = openai.chat.completions.create(
                    model="gpt-3.5-turbo",
                    messages=[
                        {
                            "role": "system",
                            "content": "너는 사용자가 제시하는 조건을 반드시 준수해서 사용자가 제시한 문장을 요약해주는 요약 전문가야."
                        },
                        {
                            "role": "user",
                            "content": "다음의 텍스트를 주제에 맞게 요약해서 json형태로 반환해줘. 단, 요약 조건은 다음과 같아.\n1.마지막에 제시될 주제에 맞추어 본문을 요약해.\n2. 절대 우선적으로 본문을 기반하는데, 그대로 넣지말고 너가 문장을 다듬어서 요약해서 넣어.\n3.한 줄로 다 적지말고, 여러 개의 text 객체로 해줘. 즉, 가능하다면 여러줄로 표현되기를 원해\n4. 다시 한 번 강조하자면, 하나의 문장이 하나의 text 객체를 이루면 좋을거 같아.\n\n" + "주제 : " + section[1] + "\n본문 : " + scriptItem[0]
                        }
                    ],
                    functions=summary_function_descriptions,
                    function_call="auto",
                    response_format={"type": "json_object"},
                    temperature=0.5,
                    top_p=1
                )
                completion_json = json.loads(completion.choices[0].message.function_call.arguments)
                for summary in completion_json['summaries']:
                    if summary['text'] :
                        cursorMySQL.execute(summarySql, (section[0], summary['text']))
    connMysql.commit()
    connMysql.close()


    return 1