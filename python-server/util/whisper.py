import os
from faster_whisper import WhisperModel


class STTResults:
    text: str
    start: float
    end: float

    def __init__(self, text: str, start: float, end: float):
        self.text = text
        self.start = start
        self.end = end


class WhisperManager:
    def __init__(self):
        model_size = "medium"
        self.model = WhisperModel(model_size, device="cuda", compute_type="float16")

    def stt(self, path: str):
        segments, info = self.model.transcribe(
            "./test3.m4a",
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
            result: STTResults = STTResults(
                text=segment.text,
                start=segment.start,
                end=segment.end
            )

            results.append(result)

        if os.path.isfile(path):
            os.remove(path)

        return results
