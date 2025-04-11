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

    def __init__(self, log_file = "resource.txt"):
        self.log_file = log_file
        self.start_time = None
        self.initial_memory = None
        self.gpu_util_samples = []
        self.cpu_util_samples = []
        self.ram_util_samples = []
        self.gpu_memory_samples = []

    def start_monitoring(self):
        """
            모니터링 시작하는 곳
        """
        gc.collect()
        self.start_time = time.time()
        process = psutil.Process(os.getpid())
        self.initial_memory = process.memory_info().rss  # 바이트 단위
        self.gpu_util_samples = []
        self.cpu_util_samples = []
        self.ram_util_samples = []
        self.gpu_memory_samples = []

    def sample_resource_usage(self):
        """
            현재 사용중인 리소스 량 샘플링
        :return:
        """
        cpu_percent = psutil.cpu_percent()
        self.cpu_util_samples.append(cpu_percent)

        ram_percent = psutil.virtual_memory().percent
        self.ram_util_samples.append(ram_percent)

        try:
            gpus = GPUtil.getGPUs() # gpu가 없을 수도 있으니 try-catch문에 넣음
            if gpus:
                gpu = gpus[0]
                self.gpu_util_samples.append(gpu.load * 100)
                self.gpu_memory_samples.append(gpu.memoryTotal)
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
        # 현재 메모리 사용량 측정
        process = psutil.Process(os.getpid())
        current_memory = process.memory_info().rss  # 바이트 단위

        # 메모리 사용량 계산 (바이트에서 MB로 변환)
        memory_used = (current_memory - self.initial_memory) / (1024 * 1024)

        # 메모리 사용량이 음수인 경우 0으로 설정 (오류 방지)
        if memory_used < 0:
            memory_used = 0

        results = {
            "processing_time_sec": processing_time,
            "memory_usage_mb": memory_used,
            "current_memory_mb": current_memory / (1024 * 1024),
            "initial_memory_mb": self.initial_memory / (1024 * 1024),
            "avg_cpu_util_percent": np.mean(self.cpu_util_samples) if self.cpu_util_samples else 0,
            "max_cpu_util_percent": np.max(self.cpu_util_samples) if self.cpu_util_samples else 0,
            "avg_ram_util_percent": np.mean(self.ram_util_samples) if self.ram_util_samples else 0,
            "max_ram_util_percent": np.max(self.ram_util_samples) if self.ram_util_samples else 0,
        }

        if self.gpu_util_samples:
            results.update({
                "avg_gpu_util_percent": np.mean(self.gpu_util_samples),
                "max_gpu_util_percent": np.max(self.gpu_util_samples),
                "avg_gpu_memory_mb": np.mean(self.gpu_memory_samples),
                "max_gpu_memory_mb": np.max(self.gpu_memory_samples)
            })

        return results

    def log_resources(self, metrics, model_info = None):
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
            f.write(f"- 메모리 사용량: {metrics['memory_usage_mb']:.2f} MB\n")
            f.write(f"- 평균 CPU 사용률: {metrics['avg_cpu_util_percent']:.2f}%\n")
            f.write(f"- 최대 CPU 사용률: {metrics['max_cpu_util_percent']:.2f}%\n")
            f.write(f"- 평균 RAM 사용률: {metrics['avg_ram_util_percent']:.2f}%\n")
            f.write(f"- 최대 RAM 사용률: {metrics['max_ram_util_percent']:.2f}%\n")

            if "avg_gpu_util_percent" in metrics:
                f.write(f"- 평균 GPU 사용률: {metrics['avg_gpu_util_percent']:.2f}%\n")
                f.write(f"- 최대 GPU 사용률: {metrics['max_gpu_util_percent']:.2f}%\n")
                f.write(f"- 평균 GPU 메모리 사용량: {metrics['avg_gpu_memory_mb']:.2f} MB\n")
                f.write(f"- 최대 GPU 메모리 사용량: {metrics['max_gpu_memory_mb']:.2f} MB\n")

            f.write("=" * 50 + "\n")

class WhisperManager:
    logger = Logger(name="consumer").logger

    def __init__(self):
        model_size = "large-v1"
        device = "cuda"
        compute_type = "float16"
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
                reference_file = "eng_master.txt"

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
            beam_size=5,
            language=lan,
            temperature=0,
            condition_on_previous_text=False,
            max_new_tokens=128,
            vad_filter=True,
            repetition_penalty=1.2,
            no_repeat_ngram_size=3,
            vad_parameters=dict(
                min_silence_duration_ms=500,
                threshold=0.5,        # VAD 감도 조정
                speech_pad_ms=400     # 음성 패딩 조정
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

