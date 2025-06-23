from typing import List

from util.whisper import WhisperManager, STTResults
from util.storage import FirebaseStorageManager
from util.open_ai import OpenAIUtil

from re import sub
import os

import librosa
import numpy as np
import soundfile as sf
from scipy import signal
import noisereduce as nr
from pydub import AudioSegment

storage_manager = FirebaseStorageManager()
whisper_manager = WhisperManager()


def stt(path: str) -> List[STTResults]:
    url = storage_manager.getDownloadUrl(path=path)
    # stt_results = whisper_manager.stt(path=url)
    stt_results = whisper_manager.stt(path=url, lan="kr")
    return stt_results


def load_reference_text(path):
    """
    참조 텍스트 파일을 로드합니다.
    파일이 없거나 오류가 발생하면 None을 반환합니다.
    """
    try:
        # 파일 이름에서 확장자를 제외한 부분 추출
        base_name = os.path.splitext(os.path.basename(path))[0]

        # 참조 텍스트 파일 경로 생성
        reference_file = f"{base_name}_master.txt"

        # 참조 텍스트 파일이 없으면 eng_master.txt 사용
        if not os.path.exists(reference_file):
            reference_file = "eng_master.txt"

        # 파일이 존재하면 내용 읽기
        if os.path.exists(reference_file):
            with open(reference_file, 'r', encoding='utf-8') as f:
                return f.read().strip()
        else:
            return None
    except Exception as e:
        return None

def stt2(path: str, lan: str) -> List[STTResults]:
    # url = storage_manager.getDownloadUrl(path=path)
    open_ai = OpenAIUtil()
    url = "./"+path+".wav" # 임시 로컬


    processed_url = preprocess_audio(url, path)

    # stt_results = whisper_manager.stt(path=url, lan=lan)
    stt_results = whisper_manager.enhance_accuracy_ensemble_stt(path=processed_url, lan=lan)
    # 참조 텍스트 로드 (WhisperManager에서 사용한 것과 동일한 방식)
    reference_text = load_reference_text(path=url)

    # 테스트 맞춤법 검사
    stt_results_enhance = open_ai.correct_spelling(stt_results=stt_results, language=lan, reference_text=reference_text)

    return stt_results_enhance


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


def preprocess_audio(audio_path: str, original_path: str) -> str:
    """
    오디오 파일을 전처리하여 STT 성능 향상을 목적으로 작성!

    Args:
        audio_path: 원본 오디오 파일 경로임
        original_path: 원본 파일 식별자

    Returns:
        전처리된 오디오 파일 경로
    """
    try:
        # 로깅
        print(f"오디오 전처리 시작: {audio_path}")

        # 전처리된 파일 저장 경로
        output_path = f"./{original_path}_processed.wav"

        # 1. 오디오 로드
        try:
            # librosa를 사용한 로드 (리샘플링 지원)
                y, sr = librosa.load(audio_path, sr=16000)  # 16kHz로 리샘플링, 8000도 가능
        except Exception as e:
            print(f"librosa 로드 실패, pydub 시도: {e}")
            # librosa 실패 시 pydub로 시도
            audio = AudioSegment.from_file(audio_path)
            audio = audio.set_channels(1)  # 모노로 변환
            audio = audio.set_frame_rate(16000)  # 16kHz로 변환
            audio.export(output_path, format="wav")
            return output_path

        # 2. 노이즈 제거
        y_reduced = nr.reduce_noise(
            y=y,
            sr=sr,
            stationary=False,  # 비정적 노이즈 가정
            prop_decrease=0.75,  # 노이즈 감소 강도
            n_fft=1024,
            win_length=512,
            hop_length=128
        )

        # 3. 정규화 (볼륨 최적화)
        y_normalized = librosa.util.normalize(y_reduced)

        # 4. 음성 강화 (선택적)
        # 저주파 및 고주파 강화 (사람 음성 주파수 대역 강화)
        b, a = signal.butter(4, [80 / sr * 2, 7500 / sr * 2], btype='band')
        y_filtered = signal.filtfilt(b, a, y_normalized)

        # 5. 볼륨 증폭 (필요시)
        gain = 1.2  # 20% 증폭
        y_amplified = np.clip(y_filtered * gain, -1.0, 1.0)  # 클리핑 방지

        # 6. 침묵 구간 제거 (선택적)
        # 짧은 침묵 구간은 유지하고 긴 침묵만 제거
        non_silent_intervals = librosa.effects.split(
            y_amplified,
            top_db=30,  # 침묵 감지 임계값 (dB)
            frame_length=1024,
            hop_length=256
        )

        # 침묵 구간이 너무 많이 제거되지 않도록 조정
        if len(non_silent_intervals) > 0:
            # 침묵 구간을 짧게 유지하면서 연결
            y_without_silence = []
            last_end = 0

            for interval in non_silent_intervals:
                start, end = interval

                # 이전 구간과의 간격이 너무 크면 짧은 침묵 추가
                if start - last_end > 8000:  # 0.5초 이상 침묵
                    silence_to_keep = min(4000, start - last_end)  # 최대 0.25초 침묵 유지
                    y_without_silence.extend(y_amplified[start - silence_to_keep:end])
                else:
                    # 이전 구간과 현재 구간 사이의 모든 샘플 유지
                    y_without_silence.extend(y_amplified[last_end:end])

                last_end = end

            y_processed = np.array(y_without_silence)
        else:
            y_processed = y_amplified

        # 7. 결과 저장
        sf.write(output_path, y_processed, sr)

        print(f"오디오 전처리 완료: {output_path}")
        return output_path

    except Exception as e:
        print(f"오디오 전처리 중 오류 발생: {e}")
        # 오류 발생 시 원본 파일 반환
        return audio_path