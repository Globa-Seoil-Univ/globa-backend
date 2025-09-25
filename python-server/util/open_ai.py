from dotenv import load_dotenv
from openai import OpenAI

from model.orm import Quiz, Section, Summary, Analysis
from util.log import Logger
from util.whisper import STTResults
from langchain.text_splitter import RecursiveCharacterTextSplitter
import json
import os
from typing import List, Optional
from datetime import datetime
from util.whisper import WhisperManager

load_dotenv()

api_key = os.environ.get("openai-api-key")

client = OpenAI(api_key=api_key)

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

    def get_qa(self, record_id: int, question: str, lan: str = 'ko'):
        try:
            # 언어별 시스템 프롬프트
            system_prompts = {
                'ko': "너는 사용자가 보내주는 2번째 줄부터 시작하는 내용을 보고 O/X 퀴즈를 여러 개 만들어주는 QA 모델이야.\n"
                      + "대화에서 자주 언급되는 내용으로만 질문을 구성해주고, 의문형으로 작성 및 정답은 골고루 내줘 "
                      + "무조건 O/X 퀴즈에 맞는 질문으로 만들어줘야 해 "
                      + "언어는 무조건 보여주는 내용 언어로만 구성이 되어야해 "
                      + "질문은 다음과 같이 예시를 들 수 있어. ex) 회의 내용의 중심적인 내용 중에는 디자인과 관련이 있다?",

                'en': "You are a QA model that creates multiple O/X quizzes based on the content starting from the second line that the user sends.\n"
                      + "Create questions only about frequently mentioned content in the conversation, write them in interrogative form, and provide balanced correct answers "
                      + "You must create questions that fit the O/X quiz format "
                      + "The language must be composed only in the same language as the content shown "
                      + "Questions can be exemplified as follows: ex) The central content of the meeting is related to design?",

                'ja': "あなたはユーザーが送信する2行目から始まる内容を見てO/Xクイズを複数作成するQAモデルです。\n"
                      + "会話でよく言及される内容のみで質問を構成し、疑問形で作成し、正答をバランスよく提供してください "
                      + "必ずO/Xクイズに適した質問を作成する必要があります "
                      + "言語は必ず表示される内容の言語のみで構成される必要があります "
                      + "質問は次のように例示できます。例）会議内容の中心的な内容にはデザインに関連するものがありますか？"
            }

            summary_prompts = {
                'ko': "너는 사용자가 보내주는 내용을 보고 간단한 요약을 해주는 모델이야.",
                'en': "You are a model that provides simple summaries of the content sent by users.",
                'ja': "あなたはユーザーが送信する内容を見て簡単な要約を提供するモデルです。"
            }

            user_prompts = {
                'ko': "다음 줄부터 보여주는 내용을 기반으로 O/X 퀴즈를 만들어서 json 형태로 반환해줘. \n\n",
                'en': "Based on the content shown from the next line, create O/X quizzes and return them in JSON format. \n\n",
                'ja': "次の行から表示される内容に基づいてO/Xクイズを作成し、JSON形式で返してください。 \n\n"
            }

            chunks = chunk_string(text=question)
            results = []

            if len(chunks) == 1:
                completion = self.client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[
                        {"role": "system", "content": system_prompts[lan]},
                        {"role": "user", "content": user_prompts[lan] + chunks[0]}
                    ],
                    functions=self.qa_function_descriptions,
                    function_call="auto",
                    response_format={"type": "json_object"},
                    temperature=0.6,
                    top_p=1
                )

                results.append(json.loads(completion.choices[0].message.function_call.arguments))
                self.logger.info(f"qa : {results[0]}")
            else:
                for i in range(0, len(chunks)):
                    prev_text = ""

                    if i > 0:
                        completion = self.client.chat.completions.create(
                            model="gpt-4o-mini",
                            messages=[
                                {"role": "system", "content": summary_prompts[lan]},
                                {"role": "user", "content": chunks[i - 1]}
                            ],
                            temperature=0.5,
                            top_p=1
                        )

                        prev_text = completion.choices[0].message.content

                    completion = self.client.chat.completions.create(
                        model="gpt-4o-mini",
                        messages=[
                            {"role": "system", "content": system_prompts[lan]},
                            {"role": "assistant", "content": prev_text},
                            {"role": "user", "content": user_prompts[lan] + chunks[i]}
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

    def get_section(self, record_id: int, stt: List[STTResults], language: str):
        section_list = []
        prev_summary = ""  # 이전 요약 저장
        all_completions = []  # 모든 completion 결과를 저장할 리스트

        self.logger.info("텍스트 분할 시작")

        # 언어별 시스템 프롬프트 설정
        system_prompts = {
            'ko': """너는 다음 조건을 반드시 준수해서 사용자가 제시한 문장 2번째 줄부터 섹션을 분리하여 알려주는 모델이야.
    1. 시간의 순서대로 흘러가야해
    2. 각 주제별로 영역을 나눠야해 이떄 주제는 너가 판단하기에 중요한 내용으로 요약해서 주제로 적어. 단, 하나의 섹션에 해당하는 문장이 최소 3문장 이상으로 구성되어야 해. 그리고, 중요하지 않다고 판단되는 내용은 섹션에 해당되는 내용에 제외시켜.
    3. 제일 중요한 조건이야. 각 파트의 시작시간과 종료시간을 반드시 적어주어야만해.
    4. 각 요약된 파트는 start시간과 end시간을 적어주어야해
    5. 각 문장의 맨 뒤에 아스타리크(*)로 감쌓여 있는 숫자는 앞에서부터 시작시간, 종료시간이야. 시간을 이것으로 판단해
    6. 각 영역은 다음의 형식을 꼭 지켜줘 ! 주제 - 시작시간, 종료시간
    7. 파트의 종료 시간은 다음 파트의 시작시간 이전이어야만해.
    8. 추임새가 반복되는 단어가 있으면 섹션 분리에서 제외시켜줘. 예시는 다음과 같아. ex) 하 하하 하하하 하""",

            'en': """You are a model that separates sections from the sentences starting from the second line presented by the user, following these conditions strictly:
    1. Must flow in chronological order
    2. Must divide by topic areas, where topics should be summarized as important content as you judge. However, sentences corresponding to one section must consist of at least 3 sentences. Also, exclude content deemed unimportant from the section content.
    3. This is the most important condition. You must include the start time and end time of each part.
    4. Each summarized part must include start time and end time
    5. The numbers enclosed in asterisks (*) at the end of each sentence are start time and end time from the front. Judge time based on this
    6. Each area must follow this format! Topic - Start time, End time
    7. The end time of a part must be before the start time of the next part.
    8. If there are repetitive filler words, exclude them from section separation. Example: ex) ha haha hahaha ha""",

            'ja': """あなたはユーザーが提示した文章の2行目から始まるセクションを分離して教えるモデルで、以下の条件を必ず守ってください：
    1. 時間の順序通りに流れなければならない
    2. 各トピック別に領域を分けなければならない。このときトピックはあなたが判断する重要な内容で要約してトピックとして書いてください。ただし、1つのセクションに該当する文章は最低3文以上で構成されなければならない。そして、重要でないと判断される内容はセクションに該当する内容から除外してください。
    3. 最も重要な条件です。各パートの開始時間と終了時間を必ず記載しなければならない。
    4. 各要約されたパートはstart時間とend時間を記載しなければならない
    5. 各文章の最後にアスタリスク(*)で囲まれた数字は前から開始時間、終了時間です。これで時間を判断してください
    6. 各領域は次の形式を必ず守ってください！トピック - 開始時間、終了時間
    7. パートの終了時間は次のパートの開始時間より前でなければならない。
    8. 相槌が繰り返される単語があればセクション分離から除外してください。例：ex) は はは ははは は"""
        }

        summary_prompts = {
            'ko': "너는 사용자가 보내주는 내용을 보고 간단한 요약을 해주는 모델이야.",
            'en': "You are a model that provides simple summaries of the content sent by users.",
            'ja': "あなたはユーザーが送信する内容を見て簡単な要約を提供するモデルです。"
        }

        user_prompts = {
            'ko': {
                'previous_summary': "이전 내용 요약: ",
                'no_previous': "이전 내용 없음",
                'additional_summary': "\n추가로 요약할 내용 : ",
                'section_request': "다음의 텍스트를 섹션으로 분리하고, 한 문장으로 주제를 만들고, 시작시간, 종료시간을 json형태로 반환해줘. \n\n"
            },
            'en': {
                'previous_summary': "Previous content summary: ",
                'no_previous': "No previous content",
                'additional_summary': "\nAdditional content to summarize: ",
                'section_request': "Separate the following text into sections, create topics in one sentence, and return start time and end time in JSON format. \n\n"
            },
            'ja': {
                'previous_summary': "前の内容の要約: ",
                'no_previous': "前の内容なし",
                'additional_summary': "\n追加で要約する内容: ",
                'section_request': "次のテキストをセクションに分離し、一文でトピックを作成し、開始時間と終了時間をJSON形式で返してください。 \n\n"
            }
        }

        # STT 결과를 텍스트로 변환
        full_text = ""
        for item in stt:
            full_text += item.text + "*" + str(item.start) + "," + str(item.end) + "*" + "\n"

        # LangChain의 RecursiveCharacterTextSplitter 사용
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=10000,
            chunk_overlap=200,  # 약간의 오버랩을 두어 문맥 유지
            length_function=len,
            separators=["\n\n", "\n", ".", "!", "?", ",", " ", ""]  # 분할 우선순위
        )

        # 텍스트를 청크로 분할
        text_chunks = text_splitter.split_text(full_text)

        self.logger.info(f"총 {len(text_chunks)}개의 청크로 분할됨")

        prev_str = ""

        section_list = [] # 청크 처리 루프 밖으로 이동 0923

        for i, current_str in enumerate(text_chunks):
            self.logger.info(f"청크 {i + 1}/{len(text_chunks)} 처리 중 (길이: {len(current_str)}자)")

            if prev_str != "":
                # 이전 내용 요약
                completion = self.client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[
                        {"role": "system", "content": summary_prompts[language]},
                        {"role": "user",
                         "content": f"{user_prompts[language]['previous_summary']}{prev_summary}" if prev_summary else
                         user_prompts[language]['no_previous']},
                        {"role": "user", "content": user_prompts[language]['additional_summary'] + prev_str},
                    ],
                    temperature=0.5,
                    top_p=1
                )

                prev_text = completion.choices[0].message.content
                prev_summary = completion.choices[0].message.content
            else:
                prev_text = ""

            self.logger.info("섹션 분리 요청 시작")

            # 섹션 분리 요청
            completion = self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "system",
                        "content": system_prompts[language]
                    },
                    {"role": "assistant", "content": prev_text},
                    {"role": "user",
                     "content": f"{user_prompts[language]['previous_summary']}{prev_summary}" if prev_summary else
                     user_prompts[language]['no_previous']},
                    {
                        "role": "user",
                        "content": user_prompts[language]['section_request'] + current_str
                    }
                ],
                functions=self.section_function_descriptions,
                function_call="auto",
                response_format={"type": "json_object"},
                temperature=0.5,
                top_p=1
            )

            self.logger.info(f"섹션 분리 완료 - 청크 {i + 1}")


            # completion 결과 저장
            completion_data = {
                "content": completion.choices[0].message.content,
                "function_call": completion.choices[0].message.function_call.arguments if hasattr(
                    completion.choices[0].message, 'function_call') else None
            }
            all_completions.append(completion_data)

            with open(str(record_id) + ".json", "w", encoding="utf-8") as f:
                json.dump(all_completions, f, ensure_ascii=False, indent=2)

            # 🎯 수정: 현재 completion만 처리 (completion_data 또는 completion 객체)
            try:
                completion_json = None

                # completion 객체에서 직접 처리 (더 안전함)
                if hasattr(completion.choices[0].message, 'function_call') and completion.choices[
                    0].message.function_call:
                    completion_json = json.loads(completion.choices[0].message.function_call.arguments)

                # 또는 completion_data에서 처리
                elif completion_data['function_call']:
                    completion_json = json.loads(completion_data['function_call'])

                # JSON 파싱 성공시 섹션 처리
                if completion_json and 'sections' in completion_json:
                    sections_data = completion_json['sections']
                    self.logger.info(f"청크 {i + 1}에서 발견된 섹션 개수: {len(sections_data)}")

                    for j, section in enumerate(sections_data):
                        if section and all(key in section for key in ['subject', 'start', 'end']):
                            try:
                                section_entity = Section(
                                    record_id=record_id,
                                    title=section['subject'],
                                    start_time=float(section['start']),
                                    end_time=float(section['end'])
                                )
                                section_list.append(section_entity)  # 여기서 추가!
                                self.logger.info(f"✅ 섹션 추가: '{section['subject']}'")
                            except (ValueError, TypeError) as e:
                                self.logger.warning(f"❌ 섹션 생성 오류: {e}")

            except json.JSONDecodeError as e:
                self.logger.warning(f"❌ 청크 {i + 1} JSON 파싱 오류: {e}")
            except Exception as e:
                self.logger.error(f"❌ 청크 {i + 1} 처리 오류: {e}")

            prev_str = current_str

        return section_list

    # 위에서 분리된 섹션에 텍스트 전문을 할당해서 Analysis 테이블에 insert
    def assign_text(self, stt_origin: List[STTResults], sections: List[Section]):
        assign_text_list = []
        assign_results = []  # 결과를 저장할 리스트
        self.logger.info("assign 진입")
        start_index = 0
        for section in sections:
            # 디버깅1
            self.logger.info(section)
            current_str = ""  # 현재 섹션의 텍스트를 저장할 변수
            text_assigned = False  # 해당 섹션에 텍스트가 할당되었는지 여부 확인

            if section:
                section_result = {
                    "section_id": section.section_id,
                    "title": section.title,
                    "start_time": section.start_time,
                    "end_time": section.end_time,
                    "content": ""
                }
                for i in range(start_index, len(stt_origin)):
                    if stt_origin[i].start <= section.end_time:
                        current_str += stt_origin[i].text  # 시간 범위 내의 텍스트 추가
                    else:
                        if current_str.strip():  # 텍스트가 있으면 저장
                            script_entity = Analysis(section_id=section.section_id, content=current_str.strip())
                            assign_text_list.append(script_entity)
                            section_result["content"] = current_str.strip()
                            text_assigned = True  # 텍스트가 할당된 것으로 플래그 설정
                        else:
                            script_entity = Analysis(section_id=section.section_id, content="")
                            assign_text_list.append(script_entity)
                            section_result["content"] = ""
                            text_assigned = True  # 빈 텍스트가라도 할당된 것으로 플래그 설정

                        start_index = i  # start_index 업데이트
                        current_str = ""
                        break  # 섹션 처리 완료 후 루프 탈출

                # 루프가 끝난 후, 텍스트가 할당되지 않았으면 처리
                if not text_assigned and current_str.strip():
                    script_entity = Analysis(section_id=section.section_id, content=current_str.strip())
                    assign_text_list.append(script_entity)
                    section_result["content"] = current_str.strip()

                # 결과 리스트에 추가
                assign_results.append(section_result)

        # 결과를 JSON 파일로 저장
        with open("assign_result.json", "w", encoding="utf-8") as f:
            json.dump(assign_results, f, ensure_ascii=False, indent=2)

        return assign_text_list, assign_results

    # section과 script를 불러와서 매칭시켜서, 요약하고, summary insert
    def get_summary(self, datas,section_list, language: str = 'ko'):
        self.logger.info("summary 진입 ============")
        summary_list = []

        # 언어별 시스템 프롬프트 설정
        system_prompts = {
            'ko': "너는 사용자가 제시하는 조건을 반드시 준수해서 사용자가 제시한 문장을 요약해주는 요약 전문가야.",
            'en': "You are a summarization expert who summarizes the sentences presented by users while strictly following the conditions they provide.",
            'ja': "あなたはユーザーが提示する条件を必ず守って、ユーザーが提示した文章を要約する要約専門家です。"
        }

        # 언어별 사용자 프롬프트 설정
        user_prompts = {
            'ko': """다음의 텍스트를 주제에 맞게 요약해서 json형태로 반환해줘. 단, 요약 조건은 다음과 같아.
    1. 마지막에 제시될 주제에 맞추어 본문을 요약해.
    2. 절대 우선적으로 본문을 기반하는데, 그대로 넣지말고 너가 문장을 다듬어서 요약해서 넣어.
    3. 한 줄로 다 적지말고, 여러 개의 text 객체로 해줘. 즉, 가능하다면 여러줄로 표현되기를 원해
    4. 다시 한 번 강조하자면, 하나의 문장이 하나의 text 객체를 이루면 좋을거 같아.

    주제: {title}
    본문: {content}""",

            'en': """Summarize the following text according to the topic and return it in JSON format. The summarization conditions are as follows:
    1. Summarize the main text according to the topic that will be presented at the end.
    2. Absolutely base it on the main text first, but don't put it as is - refine and summarize the sentences yourself.
    3. Don't write everything in one line, use multiple text objects. In other words, I want it to be expressed in multiple lines if possible.
    4. To emphasize again, it would be good if one sentence forms one text object.

    Topic: {title}
    Content: {content}""",

            'ja': """次のテキストをトピックに合わせて要約し、JSON形式で返してください。要約条件は以下の通りです：
    1. 最後に提示されるトピックに合わせて本文を要約してください。
    2. 絶対に優先的に本文を基にしますが、そのまま入れずにあなたが文章を整えて要約して入れてください。
    3. 一行で全て書かずに、複数のtextオブジェクトにしてください。つまり、可能であれば複数行で表現されることを望みます。
    4. もう一度強調しますが、一つの文章が一つのtextオブジェクトを構成すると良いと思います。

    トピック: {title}
    本文: {content}"""
        }

        for data, section in zip(datas, section_list):
            if data and (len(datas) == len(section_list)):
                if data.get('content'):
                    try:
                        # 언어에 맞는 프롬프트 생성
                        user_content = user_prompts[language].format(
                            title=data.get('title',''),
                            content=data.get('content','')
                        )

                        completion = self.client.chat.completions.create(
                            model="gpt-4o-mini",
                            messages=[
                                {
                                    "role": "system",
                                    "content": system_prompts[language]
                                },
                                {
                                    "role": "user",
                                    "content": user_content
                                }
                            ],
                            functions=self.summary_function_descriptions,
                            function_call="auto",
                            response_format={"type": "json_object"},
                            temperature=0.5,
                            top_p=1
                        )

                        # ✅ 안전한 JSON 파싱
                        completion_json = None

                        # function_call 방식 처리
                        if hasattr(completion.choices[0].message, 'function_call') and completion.choices[
                            0].message.function_call:
                            if hasattr(completion.choices[0].message.function_call, 'arguments'):
                                completion_json = json.loads(completion.choices[0].message.function_call.arguments)

                        # tool_calls 방식 처리
                        elif hasattr(completion.choices[0].message, 'tool_calls') and completion.choices[
                            0].message.tool_calls:
                            for tool_call in completion.choices[0].message.tool_calls:
                                if tool_call.function and tool_call.function.arguments:
                                    completion_json = json.loads(tool_call.function.arguments)
                                    break

                        # 일반 content 방식 처리
                        elif completion.choices[0].message.content:
                            try:
                                completion_json = json.loads(completion.choices[0].message.content)
                            except json.JSONDecodeError:
                                self.logger.warning(f"Content JSON 파싱 실패: {completion.choices[0].message.content}")
                                continue

                        if completion_json and 'summaries' in completion_json:
                            tmp_summaries: List[Summary] = []
                            for summary in completion_json['summaries']:
                                if summary.get('text'):
                                    summary_entity = Summary(
                                        section=section,
                                        content=summary['text']
                                    )
                                    summary_list.append(summary_entity)
                                    tmp_summaries.append(summary_entity)
                                    self.logger.info(f"✅ 요약 생성 성공: '{summary['text'][:50]}...'")
                            section.summaries.clear()
                            section.summaries.extend(tmp_summaries)
                        else:
                            self.logger.warning(f"❌ 요약 JSON 파싱 실패 또는 'summaries' 키 없음")

                    except json.JSONDecodeError as e:
                        self.logger.error(f"❌ JSON 파싱 오류: {e}")
                        continue
                    except Exception as e:
                        self.logger.error(f"❌ 요약 처리 오류: {e}")
                        continue

        self.logger.info(f"🎉 총 {len(summary_list)}개 요약 생성 완료")
        return summary_list, section_list

    def stt(self, path: str, lan: str):  # parameter language [ ko, ja, en ]
        self.logger.info("Starting STT2222")
        if lan == "ko":
            prompt = "너는 이제부터 한국어로 대화하는 회의, 강의, 모임 등 사람들과의 대화를 한국어 텍스트로 변환해야하는 역할이야."
        elif lan == "ja":
            prompt = "あなたは、会議、講義、会議など、人々との会話をテキストに変換する役割です。"
        else:
            prompt = "Now your role is to convert conversations from conferences, lectures, meetings, etc. into text."

        segments, info = self.model.transcribe(
            path,
            initial_prompt=prompt,
            beam_size=5,
            language=lan,
            temperature=0,
            condition_on_previous_text=False,
            max_new_tokens=128,
            vad_filter=True,
            repetition_penalty=1.2,
            no_repeat_ngram_size=3,
            vad_parameters=dict(min_silence_duration_ms=500)
        )

        results = []
        for segment in segments:
            self.logger.debug(segment)

            result: STTResults = STTResults(
                text=segment.text,
                start=segment.start,
                end=segment.end
            )

            results.append(result)

        # 맞춤법 검사 수행
        corrected_results = self.correct_spelling(results, lan)

        output_path = "./test_jp2.json"
        self.save_stt_results_to_json(corrected_results, output_path)
        self.logger.info(f"STT 결과가 {output_path}에 저장되었습니다.")

        if os.path.isfile(path):
            os.remove(path)

        return corrected_results

    def correct_spelling(self, stt_results: List[STTResults], language: str, reference_text: Optional[str] = None) -> \
    List[STTResults]:
        """
        STT 결과의 맞춤법과 오타를 수정하는 함수

        Args:
            stt_results: STT 결과 리스트
            language: 언어 코드 (ko, ja, en)

        Returns:
            맞춤법과 오타가 수정된 STT 결과 리스트
        """
        self.logger.info("맞춤법 및 오타 수정 시작")

        # 결과가 없으면 빈 리스트 반환
        if not stt_results:
            return []

        # 언어별 시스템 프롬프트 설정
        if language == "ko":
            system_prompt = """당신은 한국어 맞춤법과 오타를 수정하는 전문가입니다. 
            주어진 텍스트의 맞춤법과 오타만 수정해주세요.
            문장의 구조나 길이는 변경하지 마세요.
            원래 의미를 최대한 유지하면서 명확한 오타와 맞춤법 오류만 수정해주세요.
            수정된 텍스트만 반환해주세요."""
        elif language == "ja":
            system_prompt = """あなたは日本語の誤字脱字を修正する専門家です。
            与えられたテキストの誤字脱字だけを修正してください。
            文章の構造や長さは変更しないでください。
            元の意味を最大限に維持しながら明らかな誤字脱字だけを修正してください。
            修正されたテキストのみを返してください。"""
        else:
            system_prompt = """You are an expert in correcting English spelling and typos.
            Please correct only spelling and typos in the given text.
            Do not change the sentence structure or length.
            Maintain the original meaning as much as possible while correcting only clear typos and spelling errors.
            Return only the corrected text."""

        corrected_results = []

        # 개별 처리로 변경
        for i, item in enumerate(stt_results):
            self.logger.info(f"맞춤법 수정 중: {i + 1} / {len(stt_results)}")

            try:
                # 개별 텍스트에 대해 맞춤법 수정 요청
                response = self.client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[
                        {
                            "role": "system",
                            "content": system_prompt
                        },
                        {
                            "role": "user",
                            "content": item.text
                        }
                    ],
                    temperature=0.1,  # 더 일관된 결과를 위해 낮춤
                    max_tokens=500  # 개별 처리이므로 토큰 수 줄임
                )

                # 수정된 텍스트 가져오기
                corrected_text = response.choices[0].message.content.strip()

                # 수정된 결과를 원래 형식으로 변환 (시간 정보는 그대로 유지)
                corrected_item = STTResults(
                    text=corrected_text,
                    start=item.start,
                    end=item.end
                )
                corrected_results.append(corrected_item)

            except Exception as e:
                self.logger.error(f"항목 {i + 1} 맞춤법 수정 중 오류 발생: {str(e)}")
                # 오류 발생 시 원본 항목 추가
                corrected_results.append(item)

        # 맞춤법 수정 후 성능 지표 계산
        if reference_text:
            self.logger.info("맞춤법 수정 후 성능 지표 계산 중...")

            # WhisperManager의 calculate_metrics 함수 활용
            whisper_manager = WhisperManager()

            # 맞춤법 수정 전 성능 지표 계산
            before_metrics = whisper_manager.calculate_metrics(stt_results, reference_text)

            # 맞춤법 수정 후 성능 지표 계산
            after_metrics = whisper_manager.calculate_metrics(corrected_results, reference_text)

            # 성능 향상 계산
            improvement_metrics = {
                "corrected_wer": after_metrics["wer"],
                "corrected_cer": after_metrics["cer"],
                "corrected_bleu": after_metrics.get("bleu", 0.0),
                "wer_improvement": before_metrics["wer"] - after_metrics["wer"],
                "cer_improvement": before_metrics["cer"] - after_metrics["cer"],
                "bleu_improvement": after_metrics.get("bleu", 0.0) - before_metrics.get("bleu", 0.0)
            }

            # 리소스 모니터에 맞춤법 수정 후 지표 기록
            with open("resource.txt", 'a', encoding='utf-8') as f:
                f.write("\n[맞춤법 수정 후 성능 지표]\n")
                f.write(
                    f"- 맞춤법 수정 후 WER: {after_metrics['wer']:.4f} (개선: {improvement_metrics['wer_improvement']:.4f})\n")
                f.write(
                    f"- 맞춤법 수정 후 CER: {after_metrics['cer']:.4f} (개선: {improvement_metrics['cer_improvement']:.4f})\n")
                if "bleu" in after_metrics:
                    f.write(
                        f"- 맞춤법 수정 후 BLEU: {after_metrics['bleu']:.4f} (개선: {improvement_metrics['bleu_improvement']:.4f})\n")

            self.logger.info(f"맞춤법 수정 후 WER: {after_metrics['wer']:.4f}, CER: {after_metrics['cer']:.4f}")
            self.logger.info(
                f"성능 개선 - WER: {improvement_metrics['wer_improvement']:.4f}, CER: {improvement_metrics['cer_improvement']:.4f}")

        # 결과를 JSON 파일로 저장
        try:
            # 결과를 딕셔너리 리스트로 변환
            json_data = []
            for item in corrected_results:
                json_data.append({
                    "text": item.text,
                    "start": item.start,
                    "end": item.end
                })

            # 저장할 디렉토리 설정 (현재 작업 디렉토리 또는 지정된 경로)
            output_dir = os.path.join(os.getcwd(), "output")
            os.makedirs(output_dir, exist_ok=True)

            # 타임스탬프를 포함한 파일명 생성
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            output_file = os.path.join(output_dir, f"corrected_stt_{timestamp}.json")

            # JSON 파일로 저장
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(json_data, f, ensure_ascii=False, indent=2)

            self.logger.info(f"맞춤법 수정 결과가 {output_file}에 저장되었습니다.")
        except Exception as e:
            self.logger.error(f"결과 저장 중 오류 발생: {str(e)}")

        self.logger.info(f"맞춤법 및 오타 수정 완료 (총 {len(corrected_results)}개 항목)")

        return corrected_results


