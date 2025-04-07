from dotenv import load_dotenv
from openai import OpenAI

from model.orm import Quiz, Section, Summary, Analysis
from util.log import Logger
from util.whisper import STTResults
from langchain.text_splitter import RecursiveCharacterTextSplitter
import json
import os
from typing import List, Dict, Any
from datetime import datetime

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

    def get_qa(self, record_id: int, question: str):
        try:
            chunks = chunk_string(text=question)
            results = []

            if len(chunks) == 1:
                completion = self.client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[
                        {"role": "system",
                         "content": "너는 사용자가 보내주는 2번째 줄부터 시작하는 내용을 보고 O/X 퀴즈를 여러 개 만들어주는 QA 모델이야.\n"
                                    + "대화에서 자주 언급되는 내용으로만 질문을 구성해주고, 의문형으로 작성 및 정답은 골고루 내줘 "
                                    + "무조건 O/X 퀴즈에 맞는 질문으로 만들어줘야 해 "
                                    + "언어는 무조건 보여주는 내용 언어로만 구성이 되어야해 "
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
                self.logger.info(f"qa : {results[0]}")
            else:
                for i in range(0, len(chunks)):
                    prev_text = ""

                    if i > 0:
                        completion = self.client.chat.completions.create(
                            model="gpt-4o-mini",
                            messages=[
                                {"role": "system", "content": "너는 사용자가 보내주는 내용을 보고 간단한 요약을 해주는 모델이야."},
                                {"role": "user", "content": chunks[i - 1]}
                            ],
                            temperature=0.5,
                            top_p=1
                        )

                        prev_text = completion.choices[0].message.content

                    completion = self.client.chat.completions.create(
                        model="gpt-4o-mini",
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
        section_list = []
        prev_summary = ""  # 이전 요약 저장
        all_completions = []  # 모든 completion 결과를 저장할 리스트

        self.logger.info("텍스트 분할 시작")

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

        for i, current_str in enumerate(text_chunks):
            self.logger.info(f"청크 {i + 1}/{len(text_chunks)} 처리 중 (길이: {len(current_str)}자)")

            if prev_str != "":
                # 이전 내용 요약
                completion = self.client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[
                        {"role": "system", "content": "너는 사용자가 보내주는 내용을 보고 간단한 요약을 해주는 모델이야."},
                        {"role": "user", "content": f"이전 내용 요약 : {prev_summary}" if prev_summary else "이전 내용 없음"},
                        {"role": "user", "content": "\n추가로 요약할 내용 : " + prev_str},
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
                        "content": "너는 다음 조건을 반드시 준수해서 사용자가 제시한 문장 2번째 줄부터 섹션을 분리하여 알려주는 모델이야.\n " +
                                   "1. 시간의 순서대로 흘러가야해\n" +
                                   "2. 각 주제별로 영역을 나눠야해 이떄 주제는 너가 판단하기에 중요한 내용으로 요약해서 주제로 적어. " +
                                   "단, 하나의 섹션에 해당하는 문장이 최소 3문장 이상으로 구성되어야 해. 그리고, 중요하지 않다고 판단되는 내용은 섹션에 해당되는 내용에 제외시켜. \n" +
                                   "3. 제일 중요한 조건이야. 각 파트의 시작시간과 종료시간을 반드시 적어주어야만해. \n" +
                                   "4.각 요약된 파트는 start시간과 end시간을 적어주어야해\n" +
                                   "5.각 문장의 맨 뒤에 아스타리크(*)로 감쌓여 있는 숫자는 앞에서부터 시작시간, 종료시간이야. 시간을 이것으로 판단해\n" +
                                   "6.각 영역은 다음의 형식을 꼭 지켜줘 ! 주제 - 시작시간, 종료시간\n" +
                                   "7.파트의 종료 시간은 다음 파트의 시작시간 이전이어야만해." +
                                   "8. 추임새가 반복되는 단어가 있으면 섹션 분리에서 제외시켜줘. 예시는 다음과 같아. ex) 하 하하 하하하 하  \n\n"
                    },
                    {"role": "assistant", "content": prev_text},
                    {"role": "user", "content": f"이전 내용 요약: {prev_summary}" if prev_summary else "이전 내용 없음"},
                    {
                        "role": "user",
                        "content": "다음의 텍스트를 섹션으로 분리하고, 한 문장으로 주제를 만들고, 시작시간, 종료시간을 json형태로 반환해줘. \n\n" + current_str
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

            # 모든 completion 결과를 JSON 파일로 저장
            with open(str(record_id)+".json", "w", encoding="utf-8") as f:
                json.dump(all_completions, f, ensure_ascii=False, indent=2)

            if hasattr(completion.choices[0].message, 'function_call'):
                completion_json = json.loads(completion.choices[0].message.function_call.arguments)

                # 주석 처리된 부분 - 필요시 활성화
                # for section in completion_json['sections']:
                #     if section:
                #         section_entity = Section(record_id=record_id, title=section['subject'], start_time=section['start'], end_time=section['end'])
                #         section_list.append(section_entity)

            prev_str = current_str

        self.logger.info("모든 섹션 처리 완료")
        return section_list

    # 위에서 분리된 섹션에 텍스트 전문을 할당해서 script 테이블에 insert
    def assign_text(self, stt_origin: List[STTResults], sections: List[Section]):
        assign_text_list = []
        assign_results = []  # 결과를 저장할 리스트

        start_index = 0
        for section in sections:
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
        with open("assign_result_jp.json", "w", encoding="utf-8") as f:
            json.dump(assign_results, f, ensure_ascii=False, indent=2)

        return assign_text_list

    # section과 script를 불러와서 매칭시켜서, 요약하고, summary insert
    def get_summary(self, datas: List[Section]):
        summary_list = []
        for data in datas:
            if data:
                if data.content:
                    completion = self.client.chat.completions.create(
                        model="gpt-4o-mini",
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

    def correct_spelling(self, stt_results: List[STTResults], language: str) -> List[STTResults]:
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
            주어진 텍스트의 맞춤법과 오타를 수정해주세요. 
            각 줄의 끝에 있는 [시작:숫자, 끝:숫자] 형식의 시간 정보는 그대로 유지해야 합니다.
            텍스트 내용만 수정하고, 시간 정보는 수정하지 마세요.
            문맥을 고려하여 자연스럽게 수정해주세요.
            원래 의미를 최대한 유지하면서 수정해주세요.
            불필요한 공백이나 중복된 단어를 제거해주세요.
            구어체 특성은 유지하되, 명확한 오타와 맞춤법 오류만 수정해주세요."""
        elif language == "ja":
            system_prompt = """あなたは日本語の誤字脱字を修正する専門家です。
            与えられたテキストの誤字脱字を修正してください。
            各行の末尾にある[시작:数字, 끝:数字]形式の時間情報はそのまま維持してください。
            テキスト内容だけを修正し、時間情報は修正しないでください。
            文脈を考慮して自然に修正してください。
            元の意味を最大限に維持しながら修正してください。
            不要な空白や重複した単語を削除してください。
            口語体の特性は維持しつつ、明らかな誤字脱字だけを修正してください。"""
        else:
            system_prompt = """You are an expert in correcting English spelling and typos.
            Please correct spelling and typos in the given text.
            The time information in the format [시작:number, 끝:number] at the end of each line must be maintained.
            Only correct the text content, do not modify the time information.
            Make corrections naturally considering the context.
            Maintain the original meaning as much as possible while making corrections.
            Remove unnecessary spaces or duplicate words.
            Maintain the characteristics of spoken language, but correct only clear typos and spelling errors."""

        # 배치 크기 설정 (한 번에 처리할 STTResults 항목 수)
        BATCH_SIZE = 20  # 필요에 따라 조정

        corrected_results = []

        # 배치 단위로 처리
        for i in range(0, len(stt_results), BATCH_SIZE):
            batch = stt_results[i:i + BATCH_SIZE]
            self.logger.info(f"배치 처리 중: {i + 1}~{min(i + BATCH_SIZE, len(stt_results))} / {len(stt_results)}")

            # 배치의 모든 텍스트를 하나의 문자열로 합치기
            all_texts = []
            for item in batch:
                all_texts.append(f"{item.text} [시작:{item.start}, 끝:{item.end}]")

            combined_text = "\n".join(all_texts)

            try:
                # GPT에 맞춤법 및 오타 수정 요청
                response = self.client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[
                        {
                            "role": "system",
                            "content": system_prompt
                        },
                        {
                            "role": "user",
                            "content": f"다음 STT 결과의 맞춤법과 오타를 수정해주세요:\n\n{combined_text}"
                        }
                    ],
                    temperature=0.3,
                    max_tokens=4000
                )

                # 수정된 텍스트 파싱
                corrected_text = response.choices[0].message.content
                corrected_lines = corrected_text.strip().split("\n")

                # 수정된 결과를 원래 형식으로 변환
                batch_results = []
                for j, line in enumerate(corrected_lines):
                    if j >= len(batch):
                        break

                    # 시간 정보 추출을 위한 인덱스 찾기
                    time_start_idx = line.rfind("[시작:")
                    if time_start_idx != -1:
                        # 텍스트 부분만 추출
                        text = line[:time_start_idx].strip()

                        # 원래 시간 정보 유지
                        corrected_item = STTResults(
                            text=text,
                            start=batch[j].start,
                            end=batch[j].end
                        )
                        batch_results.append(corrected_item)
                    else:
                        # 시간 정보가 없는 경우 원본 시간 정보 사용
                        batch_results.append(STTResults(
                            text=line.strip(),
                            start=batch[j].start,
                            end=batch[j].end
                        ))

                # 배치 결과를 전체 결과에 추가
                corrected_results.extend(batch_results)

            except Exception as e:
                self.logger.error(f"배치 {i + 1}~{min(i + BATCH_SIZE, len(stt_results))} 맞춤법 수정 중 오류 발생: {str(e)}")
                # 오류 발생 시 원본 배치 결과 추가
                corrected_results.extend(batch)

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

