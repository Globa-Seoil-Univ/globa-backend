import json
from typing import List

from dotenv import load_dotenv
from openai import OpenAI

from model.orm import Quiz, Section, Summary, Analysis
from util.log import Logger
from util.whisper import STTResults

import os

load_dotenv()

api_key = os.environ.get("openai-api-key")


def chunk_string(text, chunk_size=15000):
    chunks = [text[i:i + chunk_size] for i in range(0, len(text), chunk_size)]
    return chunks


class OpenAIUtil:
    client = OpenAI(api_key=api_key)
    qa_function_descriptions = [
        {
            "name": "get_question",
            "description": "내용을 보고 퀴즈를 생성합니다.",
            "parameters": {
                "type": "object",
                "properties": {
                    "questions": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "question": {
                                    "type": "string",
                                    "description": "O/X 퀴즈에서 질문에 해당합니다."
                                },
                                "answer": {
                                    "type": "string",
                                    "enum": ["O", "X"],
                                    "description": "질문에 대한 답변을 O 또는 X로 제공합니다."
                                }
                            }
                        }
                    }
                },
                "required": ["question", "answer"],
            },
        }
    ]
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
                    "required": ["sections", "subject", "start", "end"],
                },
            }
    ]
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

    def __init__(self):
        self.logger = Logger(name="Open AI").logger
        self.client = OpenAI(api_key=api_key)

    def get_qa(self, record_id: int, question: str):
        try:
            chunks = chunk_string(text=question)
            results = []

            if len(chunks) == 1:
                completion = self.client.chat.completions.create(
                    model="gpt-3.5-turbo",
                    messages=[
                        {"role": "system",
                         "content": "너는 사용자가 보내주는 2번째 줄부터 시작하는 내용을 보고 O/X 퀴즈를 여러 개 만들어주는 QA 모델이야.\n"
                                    + "대화에서 자주 언급되는 내용으로만 질문을 구성 및 의문형으로 구성하고 정답은 골고루 내줘"
                                    + "질문은 다음과 같이 예시를 들 수 있어. ex) 회의 내용의 중심적인 내용 중에는 디자인과 관련이 있다?"},
                        {"role": "user", "content": "다음 줄부터 보여주는 내용을 기반으로 O/X 퀴즈를 만들어서 json 형태로 반환해줘. \n\n" + chunks[0]}
                    ],
                    functions=self.qa_function_descriptions,
                    function_call="auto",
                    response_format={"type": "json_object"},
                    temperature=0.6,
                    top_p=1
                )

                results.append(json.loads(completion.choices[0].message.function_call.arguments))
            else:
                for i in range(0, len(chunks)):
                    prev_text = ""

                    if i > 0:
                        completion = self.client.chat.completions.create(
                            model="gpt-3.5-turbo",
                            messages=[
                                {"role": "system", "content": "너는 사용자가 보내주는 내용을 보고 간단한 요약을 해주는 모델이야."},
                                {"role": "user", "content": chunks[i - 1]}
                            ],
                            temperature=0.5,
                            top_p=1
                        )

                        prev_text = completion.choices[0].message.content

                    completion = self.client.chat.completions.create(
                        model="gpt-3.5-turbo",
                        messages=[
                            {"role": "system",
                             "content": "너는 사용자가 보내주는 2번째 줄부터 시작하는 내용을 보고 O/X 퀴즈를 여러 개 만들어주는 QA 모델이야.\n "
                                        + "대화에서 자주 언급되는 내용으로만 질문을 구성 및 의문형으로 구성하고 정답은 골고루 내줘"
                                        + "질문은 다음과 같이 예시를 들 수 있어. ex) 회의 내용의 중심적인 내용 중에는 디자인과 관련이 있다?"},
                            {"role": "assistant", "content": prev_text},
                            {"role": "user",
                             "content": "다음 줄부터 보여주는 내용을 기반으로 O/X 퀴즈를 만들어서 json 형태로 반환해줘. \n\n" + chunks[i]}
                        ],
                        functions=self.qa_function_descriptions,
                        function_call="auto",
                        response_format={"type": "json_object"},
                        temperature=0.6,
                        top_p=1
                    )

                    results.append(
                        json.loads(completion.choices[0].message.function_call.arguments))

            quiz_list = []

            for result in results:
                for question in result['questions']:
                    quiz_question = question['question']
                    answer = True if str(question['answer']).lower() == "o" else False

                    quiz = Quiz(record_id=record_id, question=quiz_question, answer=answer)
                    quiz_list.append(quiz)

            return quiz_list
        except Exception as e:
            raise e

    def get_section(self, record_id: int, stt: List[STTResults]):
        start_index = 0

        section_list = []

        while start_index < len(stt):  # 시작 인덱스가 stt 길이보다 작은 동안 계속 반복
            current_str = ""
            for i in range(start_index, len(stt)):  # start_index부터 시작
                current_str += stt[i].text + "*" + str(stt[i].start) + "," + str(stt[i].end) + "*" + "\n"

                if len(current_str) >= 10000:  # 현재 문자열 길이가 14000을 초과하면
                    break  # 반복 중단

            completion = self.client.chat.completions.create(
                model="gpt-3.5-turbo",
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
                functions=self.section_function_descriptions,
                function_call="auto",
                response_format={"type": "json_object"},
                temperature=0.5,
                top_p=1
            )

            completion_json = json.loads(completion.choices[0].message.function_call.arguments)

            for section in completion_json['sections']:
                if section:
                    section_entity = Section(record_id=record_id, title=section['subject'], start_time=section['start'], end_time=section['end'])
                    section_list.append(section_entity)

            start_index = i + 1  # 다음 시작 인덱스 업데이트

        return section_list  # API 응답을 JSON 형태로 반환  #

    # 위에서 분리된 섹션에 텍스트 전문을 할당해서 script 테이블에 insert
    def assign_text(self, stt_origin: List[STTResults], sections: List[Section]):
        assign_text_list = []

        start_index = 0
        for section in sections:
            current_str = ""  # 현재 섹션의 텍스트를 저장할 변수

            if section:
                for i in range(start_index, len(stt_origin)):
                    if stt_origin[i].start <= section.end_time:
                        current_str += stt_origin[i].text  # 시간 범위 내의 텍스트 추가
                    elif current_str:  # 범위를 벗어나는 경우이며, current_str에 이미 텍스트가 있는 경우
                        script_entity = Analysis(section_id=section.section_id, content=current_str)
                        assign_text_list.append(script_entity)

                        start_index = i
                        current_str = ""
                        break
                    else:
                        script_entity = Analysis(section_id=section.section_id, content="")
                        assign_text_list.append(script_entity)

                        start_index = i
                        current_str = ""
                if current_str:  # 마지막으로 범위 내 텍스트가 남아 있는 경우 출력
                    script_entity = Analysis(section_id=section.section_id, content=current_str)
                    assign_text_list.append(script_entity)
        return assign_text_list

    # section과 script를 불러와서 매칭시켜서, 요약하고, summary insert
    def get_summary(self, datas: List[Section]):
        summary_list = []
        for data in datas:
            if data:
                if data.content:
                    completion = self.client.chat.completions.create(
                        model="gpt-3.5-turbo",
                        messages=[
                            {
                                "role": "system",
                                "content": "너는 사용자가 제시하는 조건을 반드시 준수해서 사용자가 제시한 문장을 요약해주는 요약 전문가야."
                            },
                            {
                                "role": "user",
                                "content": "다음의 텍스트를 주제에 맞게 요약해서 json형태로 반환해줘. 단, 요약 조건은 다음과 같아.\n1.마지막에 제시될 주제에 맞추어 본문을 요약해.\n2. 절대 우선적으로 본문을 기반하는데, 그대로 넣지말고 너가 문장을 다듬어서 요약해서 넣어.\n3.한 줄로 다 적지말고, 여러 개의 text 객체로 해줘. 즉, 가능하다면 여러줄로 표현되기를 원해\n4. 다시 한 번 강조하자면, 하나의 문장이 하나의 text 객체를 이루면 좋을거 같아.\n\n" + "주제 : " +
                                           data.title + "\n본문 : " + data.content
                            }
                        ],
                        functions= self.summary_function_descriptions,
                        function_call="auto",
                        response_format={"type": "json_object"},
                        temperature=0.5,
                        top_p=1
                    )
                    completion_json = json.loads(completion.choices[0].message.function_call.arguments)

                    for summary in completion_json['summaries']:
                        if summary['text']:
                            summary_entity = Summary(section_id=data.section_id, content=summary['text'])
                            summary_list.append(summary_entity)

        return summary_list
