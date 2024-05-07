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
        model_size = "medium"
        self.model = WhisperModel(model_size, device="cuda", compute_type="float32")

    def stt(self, path: str):
        segments, info = self.model.transcribe(
            path,
            beam_size=5,
            language="ko",
            temperature=0,
            condition_on_previous_text=False,
            max_new_tokens=128,
            vad_filter=True,
            vad_parameters=dict(min_silence_duration_ms=500)
        )

        results = []
        for segment in segments:
            # self.logger.info(f"segment: {segment}, start: {segment.start}, end: {segment}\n")
            result: STTResults = STTResults(
                text=segment.text,
                start=segment.start,
                end=segment.end
            )

            print('{'
                  f'\t"text": "{segment.text}",'
                  f'\t"start": {segment.start},'
                  f'\t"end": {segment.end},'
                  '}')

            results.append(result)

        if os.path.isfile(path):
            os.remove(path)

        return results
