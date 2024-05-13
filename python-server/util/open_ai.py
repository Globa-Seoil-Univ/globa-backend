import json
import os

from dotenv import load_dotenv
from openai import OpenAI

from model.orm import Quiz
from util.log import Logger

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
                                    + "대화에서 자주 언급되는 내용으로만 질문을 구성해야해."
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
                                        + "대화에서 자주 언급되는 내용으로만 질문을 구성해야해."
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
