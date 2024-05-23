from typing import List

from util.whisper import WhisperManager, STTResults
from util.storage import FirebaseStorageManager
from util.database import get_session

from re import sub

storage_manager = FirebaseStorageManager()
whisper_manager = WhisperManager()


def stt(path: str) -> List[STTResults]:
    url = storage_manager.getDownloadUrl(path=path)
    stt_results = whisper_manager.stt(path=url)

    return stt_results


def remove_noise_text(stt_results: List[STTResults]):
    removed_noise_results = []

    def limit_repeated_words(text: str, max_repeats: int) -> str:
        pattern = r'(\b\w+\b)(?:\s+\1){' + str(max_repeats - 1) + r',}'
        replace_pattern = r'\1' * max_repeats
        result = sub(pattern, replace_pattern, text)
        return result

    for result in stt_results:
        text = limit_repeated_words(result.text, max_repeats=4)
        removed_noise_results.append(text)
