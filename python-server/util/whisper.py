import os
from faster_whisper import WhisperModel

from util.log import Logger
import json
from dataclasses import asdict


class STTResults:
    text: str
    start: float
    end: float

    def __init__(self, text: str, start: float, end: float):
        self.text = text
        self.start = start
        self.end = end


class WhisperManager:
    logger = Logger(name="consumer").logger

    def __init__(self):
        model_size = "large-v3"
        self.model = WhisperModel(model_size, device="cuda", compute_type="float32")

    def save_stt_results_to_json(self, results, output_file_path):
        """
        STT 결과를 JSON 파일로 저장합니다.

        Args:
            results: STTResults 객체 리스트
            output_file_path: 저장할 JSON 파일 경로
        """
        # STTResults 객체를 딕셔너리로 직접 변환
        results_dict = []
        for result in results:
            results_dict.append({
                "text": result.text,
                "start": result.start,
                "end": result.end
            })

        # JSON 파일로 저장
        with open(output_file_path, 'w', encoding='utf-8') as f:
            json.dump(results_dict, f, ensure_ascii=False, indent=2)

        return output_file_path

    def stt(self, path: str, lan: str):# parameter language [ ko, ja, en ]
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

        output_path = "./enhance_stt.json"
        self.save_stt_results_to_json(results, output_path)
        self.logger.info(f"STT 결과가 {output_path}에 저장되었습니다.")

        if os.path.isfile(path):
            os.remove(path)

        return results


    # def stt(self, path: str): # default korean
    #     segments, info = self.model.transcribe(
    #         path,
    #         initial_prompt="너는 이제부터 한국어로 대화하는 회의, 강의, 모임 등 사람들과의 대화를 한글로 변환해야 하는 역할이야.",
    #         beam_size=5,
    #         language="ko",
    #         temperature=0,
    #         condition_on_previous_text=False,
    #         max_new_tokens=128,
    #         vad_filter=True,
    #         repetition_penalty=1.2,
    #         no_repeat_ngram_size=3,
    #         vad_parameters=dict(min_silence_duration_ms=500)
    #     )
    #
    #     results = []
    #     for segment in segments:
    #         self.logger.debug(segment)
    #
    #         result: STTResults = STTResults(
    #             text=segment.text,
    #             start=segment.start,
    #             end=segment.end
    #         )
    #
    #         results.append(result)
    #
    #     if os.path.isfile(path):
    #         os.remove(path)
    #
    #     return results
