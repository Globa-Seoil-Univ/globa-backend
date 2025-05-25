import os
import gc
import time
import json
import psutil
import GPUtil
import numpy as np
from faster_whisper import WhisperModel
from dataclasses import asdict
from jiwer import wer, cer
from nltk.translate.bleu_score import sentence_bleu
from typing import List, Optional

from psutil import cpu_percent

from util.log import Logger

class STTResults:
    text: str
    start: float
    end: float

    def __init__(self, text: str, start: float, end: float):
        self.text = text
        self.start = start
        self.end = end


class ResourceMonitor:
    """
        리소스 사용량 기록하기 위한 클래스
    """

    def __init__(self, log_file="resource.txt"):
        self.log_file = log_file
        self.start_time = None
        self.gpu_util_samples = []
        self.cpu_util_samples = []
        self.ram_util_samples = []
        self.gpu_memory_samples = []
        self.memory_samples = []  # 메모리 사용량 샘플 잠깐 추가

    def start_monitoring(self):
        """
            모니터링 시작하는 곳
        """
        gc.collect()
        self.start_time = time.time()
        self.gpu_util_samples = []
        self.cpu_util_samples = []
        self.ram_util_samples = []
        self.gpu_memory_samples = []
        self.memory_samples = []  # 메모리 샘플 초기화ㅏ

    def sample_resource_usage(self):
        """
            현재 사용중인 리소스 량 샘플링
        :return:
        """
        # CPU 사용률
        cpu_percent = psutil.cpu_percent()
        self.cpu_util_samples.append(cpu_percent)

        # RAM 사용률
        ram_percent = psutil.virtual_memory().percent
        self.ram_util_samples.append(ram_percent)

        # 현재 프로세스의 메모리 사용량 (MB 단위)
        process = psutil.Process(os.getpid())
        current_memory = process.memory_info().rss / (1024 * 1024)  # MB로 변환
        self.memory_samples.append(current_memory)

        # GPU 사용량
        try:
            gpus = GPUtil.getGPUs()  # gpu가 없을 수도 있으니 try-catch문에 넣음
            if gpus:
                gpu = gpus[0]
                self.gpu_util_samples.append(gpu.load * 100)
                self.gpu_memory_samples.append(gpu.memoryUsed)  # 기존에 memoryTotal을 써서 자꾸 최대 값이 나왔던 거였음!
        except Exception as e:
            print(f"GPU 모니터링 에러 : {e}")

    def stop_monitoring(self):
        """
            모니터링 종료 및 결과 저장
        :return:
        """
        # 가비지 컬렉션 강제 실행
        gc.collect()
        processing_time = time.time() - self.start_time

        # 메모리 사용량 계산 (평균, 최대값)
        if self.memory_samples:
            avg_memory = np.mean(self.memory_samples)
            max_memory = np.max(self.memory_samples)
            initial_memory = self.memory_samples[0] if self.memory_samples else 0
            peak_memory_usage = max_memory - initial_memory
        else:
            avg_memory = 0
            max_memory = 0
            initial_memory = 0
            peak_memory_usage = 0

        results = {
            "processing_time_sec": processing_time,
            "avg_memory_mb": avg_memory,
            "max_memory_mb": max_memory,
            "peak_memory_usage_mb": peak_memory_usage if peak_memory_usage > 0 else 0,
            "avg_cpu_util_percent": np.mean(self.cpu_util_samples) if self.cpu_util_samples else 0,
            "max_cpu_util_percent": np.max(self.cpu_util_samples) if self.cpu_util_samples else 0,
            "avg_ram_util_percent": np.mean(self.ram_util_samples) if self.ram_util_samples else 0,
            "max_ram_util_percent": np.max(self.ram_util_samples) if self.ram_util_samples else 0,
        }

        if self.gpu_util_samples:
            results.update({
                "avg_gpu_util_percent": np.mean(self.gpu_util_samples),
                "max_gpu_util_percent": np.max(self.gpu_util_samples),
                "avg_gpu_memory_used_mb": np.mean(self.gpu_memory_samples),
                "max_gpu_memory_used_mb": np.max(self.gpu_memory_samples)
            })

        return results

    def log_resources(self, metrics, model_info=None):
        """ 파일에 기록하기 """
        with open(self.log_file, 'a', encoding='utf-8') as f:
            f.write("\n" + "=" * 50 + "\n")
            f.write(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] STT 처리 결과\n")

            if model_info:
                f.write(f"\n[모델 정보]\n")
                for key, value in model_info.items():
                    f.write(f"- {key}: {value}\n")

            f.write(f"\n[성능 지표]\n")
            for key, value in metrics.items():
                if key.startswith(("wer", "cer", "bleu")):
                    f.write(f"- {key}: {value:.4f}\n")

            f.write(f"\n[리소스 사용량]\n")
            f.write(f"- 처리 시간: {metrics['processing_time_sec']:.2f} 초\n")
            f.write(f"- 평균 메모리 사용량: {metrics['avg_memory_mb']:.2f} MB\n")
            f.write(f"- 최대 메모리 사용량: {metrics['max_memory_mb']:.2f} MB\n")
            f.write(f"- 피크 메모리 사용량: {metrics['peak_memory_usage_mb']:.2f} MB\n")
            f.write(f"- 평균 CPU 사용률: {metrics['avg_cpu_util_percent']:.2f}%\n")
            f.write(f"- 최대 CPU 사용률: {metrics['max_cpu_util_percent']:.2f}%\n")
            f.write(f"- 평균 RAM 사용률: {metrics['avg_ram_util_percent']:.2f}%\n")
            f.write(f"- 최대 RAM 사용률: {metrics['max_ram_util_percent']:.2f}%\n")

            if "avg_gpu_util_percent" in metrics:
                f.write(f"- 평균 GPU 사용률: {metrics['avg_gpu_util_percent']:.2f}%\n")
                f.write(f"- 최대 GPU 사용률: {metrics['max_gpu_util_percent']:.2f}%\n")
                f.write(f"- 평균 GPU 메모리 사용량: {metrics['avg_gpu_memory_used_mb']:.2f} MB\n")
                f.write(f"- 최대 GPU 메모리 사용량: {metrics['max_gpu_memory_used_mb']:.2f} MB\n")

            f.write("=" * 50 + "\n")

class WhisperManager:
    logger = Logger(name="consumer").logger

    def __init__(self):
        model_size = "medium"
        device = "cuda"
        compute_type = "float32"
        cpu_threads = 16
        num_workers = 8

        self.model = WhisperModel(model_size, device=device, compute_type=compute_type, cpu_threads=cpu_threads, num_workers=num_workers)

        self.resource_monitor = ResourceMonitor(log_file="resource.txt")
        self.model_info = {
            "model_size": model_size,
            "device": device,
            "compute_type": compute_type,
            "cpu_threads": cpu_threads,
            "num_workers": num_workers
        }

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
        # json_data = {
        #     "result": results_dict
        # }
        # JSON 파일로 저장
        with open(output_file_path, 'w', encoding='utf-8') as f:
            json.dump(results_dict, f, ensure_ascii=False, indent=2)

        return output_file_path

    def calculate_metrics(self, results: List[STTResults], reference_text: Optional[str] = None) -> dict:
        """
        STT 결과의 성능 지표를 계산합니다.

        Args:
            results: STTResults 객체 리스트
            reference_text: 참조 텍스트 (있는 경우)

        Returns:
            dict: 성능 지표 딕셔너리
        """
        metrics = {}

        # 전체 텍스트 결합
        hypothesis_text = " ".join([result.text.strip() for result in results])

        # 참조 텍스트가 있는 경우 WER, CER, BLEU 계산
        if reference_text:
            metrics["wer"] = wer(reference_text, hypothesis_text)
            metrics["cer"] = cer(reference_text, hypothesis_text)

            # BLEU 점수 계산 (단어 단위)
            reference_words = [reference_text.split()]
            hypothesis_words = hypothesis_text.split()
            try:
                metrics["bleu"] = sentence_bleu(reference_words, hypothesis_words)
            except Exception as e:
                self.logger.error(f"BLEU 점수 계산 오류: {e}")
                metrics["bleu"] = 0.0

        return metrics

    def load_reference_text(self, path):
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
                reference_file = "gisa.txt"

            # 파일이 존재하면 내용 읽기
            if os.path.exists(reference_file):
                with open(reference_file, 'r', encoding='utf-8') as f:
                    return f.read().strip()
            else:
                self.logger.warning(f"참조 텍스트 파일을 찾을 수 없습니다: {reference_file}")
                return None
        except Exception as e:
            self.logger.error(f"참조 텍스트 로드 중 오류 발생: {e}")
            return None

    def stt(self, path: str, lan: str):# parameter language [ ko, ja, en ]
        """
        음성을 텍스트로 변환하고 성능 지표와 리소스 사용량을 측정합니다.

        Args:
            path: 오디오 파일 경로
            lan: 언어 코드 (ko, ja, en)
            reference_text: 참조 텍스트 (성능 평가용, 선택사항)

        Returns:
            List[STTResults]: STT 결과 객체 리스트
        """
        self.logger.info("Starting STT with resource monitoring")


        # 참조 텍스트 로드
        reference_text = self.load_reference_text(path)
        if reference_text:
            self.logger.info("참조 텍스트를 로드했습니다. 성능 지표를 계산합니다.")
        else:
            self.logger.info("참조 텍스트를 찾을 수 없습니다. 성능 지표는 계산하지 않습니다.")
        # 리소스 모니터링 시작
        self.resource_monitor.start_monitoring()


        if lan == "ko":
            prompt = "너는 이제부터 한국어로 대화하는 회의, 강의, 모임 등 사람들과의 대화를 한국어 텍스트로 변환해야하는 역할이야."
        elif lan == "ja":
            prompt = "あなたは、会議、講義、会議など、人々との会話をテキストに変換する役割です。"
        else:
            prompt = "Now your role is to convert conversations from conferences, lectures, meetings, etc. into text."

        import threading
        stop_monitoring = False

        def monitor_resources():
            while not stop_monitoring:
                self.resource_monitor.sample_resource_usage()
                time.sleep(0.5)  # 0.5초마다 샘플링

        monitor_thread = threading.Thread(target=monitor_resources)
        monitor_thread.daemon = True
        monitor_thread.start()

        # STT 수행
        segments, info = self.model.transcribe(
            path,
            initial_prompt=prompt,
            beam_size=7,
            language=lan,
            temperature=0,
            condition_on_previous_text=True,
            max_new_tokens=128, # 합 448 미만
            vad_filter=True,
            repetition_penalty=1.2,
            no_repeat_ngram_size=3,
            vad_parameters=dict(
                min_silence_duration_ms=500,
                threshold=0.5,        # VAD 감도 조정
                speech_pad_ms=200     # 음성 패딩 조정
            ),
            best_of=5,  # 가장 좋은 결과 선택 (beam_size와 함께 조정)
            suppress_blank=True,      # 빈 세그먼트 억제
            suppress_tokens=[-1],     # 특수 토큰 억제
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
        # 모니터링 중지
        stop_monitoring = True
        monitor_thread.join(timeout=1.0)

        # 리소스 모니터링 종료 및 결과 수집
        resource_metrics = self.resource_monitor.stop_monitoring()

        # 성능 지표 계산 (참조 텍스트가 있는 경우)
        if reference_text:
            performance_metrics = self.calculate_metrics(results, reference_text)
            all_metrics = {**resource_metrics, **performance_metrics}
        else:
            all_metrics = resource_metrics

        # 지표 기록
        self.resource_monitor.log_resources(all_metrics, self.model_info)

        # ST 파일 결과 저장
        file_name = os.path.basename(path)
        output_path = f"./origin_{file_name}.json"
        self.save_stt_results_to_json(results, output_path)
        self.logger.info(f"STT 결과가 {output_path}에 저장되었습니다.")

        # 파일 제거하는건데 자꾸 귀찮으므로 잠깐 주석
        # if os.path.isfile(path):
        #     os.remove(path)

        return results
    ## 앙상블 시도1
    def init_ensemble_models(self):
        """앙상블에 사용할 여러 모델 초기화"""
        self.logger.info("앙상블 모델 초기화 중...")

        # 기본 모델은 이미 self.model에 초기화되어 있음
        # 추가 모델 초기화
        self.ensemble_models = {
            "medium": self.model,  # 기존에 초기화된 모델 재사용
        }

        # 다른 모델 크기 추가 (필요에 따라 조정)
        try:
            self.logger.info("large-v3 모델 로딩 중...")
            self.ensemble_models["large-v3"] = WhisperModel(
                "large-v3",
                device="cuda",
                compute_type="float16",  # large 모델은 메모리 절약을 위해 float16 사용
                cpu_threads=16,
                num_workers=8
            )
            self.logger.info("large-v3 모델 로딩 완료")
        except Exception as e:
            self.logger.error(f"large-v3 모델 로딩 실패: {e}")

        # 필요에 따라 더 많은 모델 추가 가능
        # self.ensemble_models["small"] = WhisperModel("small", device="cuda", compute_type="float32")

        return self.ensemble_models

    def ensemble_stt(self, path: str, lan: str):
        """여러 모델의 결과를 앙상블하여 최종 결과 생성"""
        self.logger.info("앙상블 STT 시작")

        # 앙상블 모델 초기화 (아직 초기화되지 않았다면)
        if not hasattr(self, 'ensemble_models'):
            self.init_ensemble_models()

        # 리소스 모니터링 시작
        self.resource_monitor.start_monitoring()

        # 각 모델별 결과 저장
        all_results = {}

        # 모니터링 스레드 설정
        import threading
        stop_monitoring = False

        def monitor_resources():
            while not stop_monitoring:
                self.resource_monitor.sample_resource_usage()
                time.sleep(0.5)

        monitor_thread = threading.Thread(target=monitor_resources)
        monitor_thread.daemon = True
        monitor_thread.start()

        # 각 모델로 STT 수행
        for model_name, model in self.ensemble_models.items():
            self.logger.info(f"{model_name} 모델로 STT 수행 중...")

            if lan == "ko":
                prompt = "너는 이제부터 한국어로 대화하는 회의, 강의, 모임 등 사람들과의 대화를 한국어 텍스트로 변환해야하는 역할이야."
            elif lan == "ja":
                prompt = "あなたは、会議、講義、会議など、人々との会話をテキストに変換する役割です。"
            else:
                prompt = "Now your role is to convert conversations from conferences, lectures, meetings, etc. into text."

            segments, info = model.transcribe(
                path,
                initial_prompt=prompt,
                beam_size=10,
                language=lan,
                temperature=0,
                condition_on_previous_text=True,
                max_new_tokens=128,
                vad_filter=True,
                repetition_penalty=1.2,
                no_repeat_ngram_size=3,
                vad_parameters=dict(
                    min_silence_duration_ms=500,
                    threshold=0.5,
                    speech_pad_ms=200
                ),
                best_of=9,
                suppress_blank=True,
                suppress_tokens=[-1],
            )

            # 결과 저장
            model_results = []
            for segment in segments:
                result = STTResults(
                    text=segment.text,
                    start=segment.start,
                    end=segment.end
                )
                model_results.append(result)

            all_results[model_name] = model_results
            self.logger.info(f"{model_name} 모델 처리 완료: {len(model_results)}개 세그먼트 생성")

        # 모니터링 중지
        stop_monitoring = True
        monitor_thread.join(timeout=1.0)

        # 앙상블 결과 생성
        final_results = self._combine_results(all_results)

        # 리소스 모니터링 종료 및 결과 수집
        resource_metrics = self.resource_monitor.stop_monitoring()

        # 참조 텍스트 로드 및 성능 지표 계산
        reference_text = self.load_reference_text(path)
        if reference_text:
            # 앙상블 결과 성능 계산
            performance_metrics = self.calculate_metrics(final_results, reference_text)
            all_metrics = {**resource_metrics, **performance_metrics}

            # 각 개별 모델의 성능도 계산
            for model_name, model_results in all_results.items():
                model_metrics = self.calculate_metrics(model_results, reference_text)
                all_metrics[f"{model_name}_wer"] = model_metrics["wer"]
                all_metrics[f"{model_name}_cer"] = model_metrics["cer"]

                # 로그에 각 모델 성능 출력
                self.logger.info(
                    f"{model_name} 모델 성능 - WER: {model_metrics['wer']:.4f}, CER: {model_metrics['cer']:.4f}")
        else:
            all_metrics = resource_metrics

        # 지표 기록
        model_info = {
            "ensemble_models": ", ".join(self.ensemble_models.keys()),
            "device": "cuda",
            "ensemble_method": "confidence_voting"  # 사용한 앙상블 방법
        }
        self.resource_monitor.log_resources(all_metrics, model_info)

        # 결과 저장
        file_name = os.path.basename(path)
        output_path = f"./ensemble_{file_name}.json"
        self.save_stt_results_to_json(final_results, output_path)
        self.logger.info(f"앙상블 STT 결과가 {output_path}에 저장되었습니다.")

        return final_results

    def _combine_results(self, all_results):
        """여러 모델의 결과를 조합하는 메서드"""
        self.logger.info("앙상블 결과 조합 중...")

        # 모든 세그먼트를 시간 순서로 정렬
        all_segments = []
        for model_name, results in all_results.items():
            for result in results:
                # (모델 이름, 세그먼트, 텍스트 길이) 형태로 저장
                all_segments.append((model_name, result, len(result.text)))

        # 시작 시간으로 정렬
        all_segments.sort(key=lambda x: x[1].start)

        # 겹치는 세그먼트 병합
        final_results = []
        current = None

        for model_name, segment, text_len in all_segments:
            if current is None:
                current = (model_name, segment, text_len)
                continue

            current_model, current_segment, current_len = current

            # 세그먼트가 겹치는 경우 (시간 겹침 기준)
            overlap_threshold = 0.5  # 50% 이상 겹치면 같은 세그먼트로 간주
            current_duration = current_segment.end - current_segment.start
            segment_duration = segment.end - segment.start

            overlap_start = max(current_segment.start, segment.start)
            overlap_end = min(current_segment.end, segment.end)

            if overlap_end > overlap_start:
                overlap_duration = overlap_end - overlap_start
                overlap_ratio1 = overlap_duration / current_duration
                overlap_ratio2 = overlap_duration / segment_duration

                if overlap_ratio1 > overlap_threshold or overlap_ratio2 > overlap_threshold:
                    # 겹치는 경우, 더 좋은 결과 선택
                    # 여기서는 텍스트 길이가 더 긴 것을 선택 (더 많은 정보 포함 가정)
                    # 다른 기준으로 변경 가능 (예: 특정 모델 우선, 신뢰도 점수 등)
                    if text_len > current_len:
                        current = (model_name, segment, text_len)
                    continue

            # 겹치지 않거나 겹침이 적은 경우, 현재 세그먼트 추가하고 다음으로 이동
            final_results.append(current_segment)
            current = (model_name, segment, text_len)

        # 마지막 세그먼트 추가
        if current is not None:
            final_results.append(current[1])

        self.logger.info(f"앙상블 결과 조합 완료: {len(final_results)}개 세그먼트 생성")
        return final_results

