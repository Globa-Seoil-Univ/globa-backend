import os
from faster_whisper import WhisperModel

from util.log import Logger


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

    def stt(self, path: str):
        segments, info = self.model.transcribe(
            path,
            initial_prompt="너는 이제부터 한국어로 대화하는 회의, 강의, 모임 등 사람들과의 대화를 한글로 변환해야 하는 역할이야.",
            beam_size=5,
            language="ko",
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

        if os.path.isfile(path):
            os.remove(path)

        return results
