import os


class EnhancedEnsembleSTT:
    def __init__(self, model, logger=None):
        self.model = model
        self.logger = logger or logging.getLogger(__name__)

        # 언어 모델 초기화
        self.lm = self._initialize_language_model()

        # 성능 측정 지표
        self.metrics = {
            'processed_files': 0,
            'total_duration': 0,
            'wer_before_correction': [],
            'wer_after_correction': []
        }

    def transcribe(self, path, language="ko", high_quality=True):
        """고급 앙상블 기법으로 WER 최소화"""
        self.logger.info(f"고급 앙상블 STT 시작: {os.path.basename(path)}")

        # 1. 오디오 분석 및 전처리
        enhanced_audio = self._preprocess_audio(path)

        # 2. 멀티스테이지 인식 파이프라인
        results = self._multistage_recognition(enhanced_audio, language, high_quality)

        # 3. 언어 모델 기반 교정
        corrected_results = self._apply_language_model_correction(results, language)

        # 4. 컨텍스트 기반 후처리
        final_results = self._context_based_postprocessing(corrected_results)

        # 성능 기록 (선택적)
        if hasattr(self, 'reference_text'):
            self._evaluate_wer(final_results, self.reference_text)

        return final_results

    def _preprocess_audio(self, path):
        """오디오 신호 개선을 위한 전처리"""
        self.logger.info("오디오 전처리 시작")

        try:
            import librosa
            import numpy as np
            import soundfile as sf
            import tempfile

            # 오디오 로드
            y, sr = librosa.load(path, sr=None)

            # 1. 볼륨 정규화
            y_normalized = librosa.util.normalize(y)

            # 2. 노이즈 감소 (스펙트럼 서브트랙션)
            if librosa.feature.rms(y=y_normalized).mean() < 0.05:  # 낮은 신호 강도
                self.logger.info("노이즈 감소 적용")
                y_filtered = self._apply_noise_reduction(y_normalized, sr)
            else:
                y_filtered = y_normalized

            # 3. 저주파/고주파 필터링 (음성 주파수 대역 강화)
            y_filtered = librosa.effects.preemphasis(y_filtered)

            # 4. VAD로 무음 구간 제거하고 더미 무음 추가 (인식 품질 향상)
            y_filtered = self._apply_smart_vad(y_filtered, sr)

            # 임시 파일에 저장
            with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp_file:
                sf.write(tmp_file.name, y_filtered, sr)
                return tmp_file.name

        except ImportError:
            self.logger.warning("오디오 전처리 라이브러리 없음, 원본 사용")
            return path
        except Exception as e:
            self.logger.error(f"오디오 전처리 오류: {str(e)}")
            return path

    def _apply_noise_reduction(self, y, sr):
        """노이즈 감소 알고리즘 적용"""
        import librosa
        import numpy as np

        # 스펙트로그램 계산
        D = librosa.stft(y)
        D_mag, D_phase = librosa.magphase(D)

        # 노이즈 프로파일 추정 (첫 0.5초 활용)
        noise_frames = min(int(0.5 * sr // librosa.get_window_size('hann', sr=sr)), D_mag.shape[1] // 4)
        noise_profile = np.mean(D_mag[:, :noise_frames], axis=1, keepdims=True)

        # 스펙트럼 서브트랙션
        gain = 1.0  # 노이즈 감소 강도
        D_mag_reduced = np.maximum(D_mag - gain * noise_profile, 0.01 * D_mag)

        # 역변환
        y_reduced = librosa.istft(D_mag_reduced * D_phase)

        return y_reduced

    def _apply_smart_vad(self, y, sr):
        """적응형 VAD로 무음 구간 처리"""
        import librosa
        import numpy as np

        # RMS 에너지 계산
        frame_length = int(0.025 * sr)
        hop_length = int(0.010 * sr)
        rms = librosa.feature.rms(y=y, frame_length=frame_length, hop_length=hop_length)[0]

        # 동적 임계값 계산
        threshold = np.mean(sorted(rms)[:int(len(rms) * 0.1)]) * 2.5

        # 음성/무음 프레임 식별
        speech_frames = rms > threshold

        # 짧은 무음 구간 유지 (200ms 이하)
        min_silence = int(0.2 * sr / hop_length)
        speech_regions = self._smooth_speech_regions(speech_frames, min_silence)

        # 음성 구간 추출 및 처리
        processed_audio = []
        prev_end = 0

        for start_frame, end_frame in speech_regions:
            start_sample = start_frame * hop_length
            end_sample = min(end_frame * hop_length, len(y))

            # 짧은 무음 구간 추가 (50ms)
            silence_samples = int(0.05 * sr)

            # 무음 추가 (이전 구간과 현재 구간 사이)
            if start_sample > prev_end:
                processed_audio.append(np.zeros(min(silence_samples, start_sample - prev_end)))

            # 음성 구간 추가
            processed_audio.append(y[start_sample:end_sample])
            prev_end = end_sample

        # 최종 무음 추가
        processed_audio.append(np.zeros(int(0.1 * sr)))

        return np.concatenate(processed_audio)

    def _smooth_speech_regions(self, speech_frames, min_silence):
        """음성 영역 스무딩 및 병합"""
        regions = []
        in_speech = False
        start_frame = 0

        for i, is_speech in enumerate(speech_frames):
            if is_speech and not in_speech:
                in_speech = True
                start_frame = i
            elif not is_speech and in_speech:
                # 짧은 무음은 무시하고 계속 음성 처리
                if i + min_silence < len(speech_frames) and any(speech_frames[i:i + min_silence]):
                    continue

                in_speech = False
                regions.append((start_frame, i))

        # 마지막 음성 구간 처리
        if in_speech:
            regions.append((start_frame, len(speech_frames)))

        # 인접한 영역 병합
        merged_regions = []
        if not regions:
            return merged_regions

        current_start, current_end = regions[0]

        for start, end in regions[1:]:
            if start - current_end <= min_silence:
                # 인접 영역 병합
                current_end = end
            else:
                # 새 영역 추가
                merged_regions.append((current_start, current_end))
                current_start, current_end = start, end

        merged_regions.append((current_start, current_end))
        return merged_regions

    def _multistage_recognition(self, audio_path, language, high_quality):
        """다단계 인식 파이프라인"""
        self.logger.info("다단계 인식 시작")

        # 1단계: 빠른 첫 패스 (세그멘테이션 중심)
        first_pass_config = {
            "beam_size": 1 if not high_quality else 3,
            "temperature": 0.0,
            "vad_filter": True,
            "vad_parameters": {"min_silence_duration_ms": 350, "threshold": 0.5}
        }

        first_segments, _ = self.model.transcribe(
            audio_path, language=language, **first_pass_config
        )

        # 빠른 결과 분석
        first_results = [STTResults(text=s.text, start=s.start, end=s.end) for s in first_segments]
        segment_quality = self._analyze_segment_quality(first_results)

        # 2단계: 품질 기반 집중 재인식
        focused_results = []

        for segment, quality in zip(first_results, segment_quality):
            # 저품질 세그먼트만 재인식
            if quality < 0.7:  # 품질 임계값
                self.logger.info(f"세그먼트 재인식: {segment.start:.2f}-{segment.end:.2f} (품질: {quality:.2f})")

                # 오디오 세그먼트 추출
                segment_audio = self._extract_audio_segment(
                    audio_path, segment.start, segment.end, padding_sec=0.5
                )

                if segment_audio:
                    # 고품질 설정으로 재인식
                    refined_config = {
                        "beam_size": 10 if high_quality else 5,
                        "temperature": 0.0,
                        "condition_on_previous_text": False,
                        "word_timestamps": True,
                        "prompt": self._get_context_prompt(segment, language)
                    }

                    refined_segments, _ = self.model.transcribe(
                        segment_audio, language=language, **refined_config
                    )

                    # 시간 오프셋 적용
                    adjusted_results = []
                    for s in refined_segments:
                        offset_start = max(0, segment.start - 0.5 + s.start)
                        offset_end = segment.start - 0.5 + s.end
                        adjusted_results.append(STTResults(
                            text=s.text,
                            start=offset_start,
                            end=offset_end
                        ))

                    focused_results.extend(adjusted_results)
                    continue

            # 고품질 세그먼트는 그대로 사용
            focused_results.append(segment)

        # 3단계: 전체 컨텍스트 통합 (선택적)
        if high_quality and len(focused_results) >= 3:
            self.logger.info("전체 컨텍스트 통합 수행")
            integrated_results = self._integrate_full_context(focused_results, language)
            return integrated_results

        return focused_results

    def _analyze_segment_quality(self, segments):
        """세그먼트 품질 점수 계산"""
        quality_scores = []

        for segment in segments:
            # 기본 품질 점수
            quality = 0.5

            # 1. 길이 기반 점수
            duration = segment.end - segment.start
            if 1.0 <= duration <= 15.0:
                quality += 0.2  # 이상적인 길이
            elif duration > 30.0:
                quality -= 0.3  # 비정상적으로 긴 세그먼트
            elif duration < 0.5:
                quality -= 0.1  # 너무 짧은 세그먼트

            # 2. 텍스트 내용 기반 점수
            text = segment.text.strip()
            if not text:
                quality -= 0.3  # 비어있는 텍스트
            else:
                # 단어 수 확인
                words = text.split()
                if len(words) >= 3:
                    quality += 0.1

                # 문장 종결 확인
                if any(text.endswith(c) for c in ['.', '?', '!', '。', '？', '！']):
                    quality += 0.1

                # 특수문자 비율
                import re
                special_ratio = len(re.findall(r'[^\w\s]', text)) / max(len(text), 1)
                if special_ratio > 0.3:
                    quality -= 0.2  # 특수문자 과다

            # 최종 품질 점수 (0.0-1.0 범위로 제한)
            quality_scores.append(max(0.0, min(1.0, quality)))

        return quality_scores

    def _extract_audio_segment(self, audio_path, start_sec, end_sec, padding_sec=0.5):
        """오디오 세그먼트 추출"""
        try:
            import librosa
            import soundfile as sf
            import numpy as np
            import tempfile

            # 오디오 로드
            y, sr = librosa.load(audio_path, sr=None)

            # 시간을 샘플로 변환
            start_sample = max(0, int((start_sec - padding_sec) * sr))
            end_sample = min(len(y), int((end_sec + padding_sec) * sr))

            # 세그먼트 추출
            segment = y[start_sample:end_sample]

            # 저장
            with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp_file:
                sf.write(tmp_file.name, segment, sr)
                return tmp_file.name

        except Exception as e:
            self.logger.error(f"오디오 세그먼트 추출 오류: {str(e)}")
            return None

    def _get_context_prompt(self, segment, language):
        """컨텍스트 기반 프롬프트 생성"""
        if language == "ko":
            return "다음 한국어 대화를 정확하게 받아적으세요. 특히 전문 용어와 고유명사에 주의하세요."
        elif language == "en":
            return "Transcribe the following English conversation accurately. Pay special attention to technical terms and proper nouns."
        elif language == "ja":
            return "次の日本語の会話を正確に書き起こしてください。専門用語と固有名詞に特に注意してください。"
        else:
            return "Transcribe the following conversation accurately."

    def _integrate_full_context(self, segments, language):
        """전체 컨텍스트 통합 처리"""
        # 세그먼트 그룹화 (연속된 대화)
        groups = []
        current_group = []

        for i, segment in enumerate(segments):
            if not current_group:
                current_group.append(segment)
                continue

            # 시간 간격이 4초 이상이면 새 그룹 시작
            prev_segment = current_group[-1]
            if segment.start - prev_segment.end > 4.0:
                groups.append(current_group)
                current_group = [segment]
            else:
                current_group.append(segment)

        if current_group:
            groups.append(current_group)

        # 각 그룹 내에서 컨텍스트 기반 재검토
        integrated_results = []

        for group in groups:
            if len(group) <= 2:  # 작은 그룹은 그대로 사용
                integrated_results.extend(group)
                continue

            # 그룹 전체 텍스트
            group_text = " ".join(s.text for s in group)

            # LLM 또는 언어 모델 기반 교정 (예시)
            corrected_text = self._apply_contextual_correction(group_text, language)

            # 수정된 텍스트를 원래 세그먼트에 재분배
            corrected_segments = self._redistribute_text(corrected_text, group)
            integrated_results.extend(corrected_segments)

        return integrated_results

    def _initialize_language_model(self):
        """언어 모델 초기화 (KenLM 또는 다른 경량 LM)"""
        try:
            import kenlm

            # 미리 훈련된 언어 모델 로드 (이미 있다고 가정)
            model_paths = {
                'ko': 'models/ko.arpa.bin',
                'en': 'models/en.arpa.bin',
                'ja': 'models/ja.arpa.bin'
            }

            models = {}
            for lang, path in model_paths.items():
                if os.path.exists(path):
                    models[lang] = kenlm.Model(path)
                    self.logger.info(f"{lang} 언어 모델 로드 완료")

            if not models:
                self.logger.warning("언어 모델을 찾을 수 없음, 언어 모델 없이 진행")

            return models

        except ImportError:
            self.logger.warning("KenLM을 찾을 수 없음, 언어 모델 없이 진행")
            return {}

    def _apply_language_model_correction(self, segments, language):
        """언어 모델 기반 텍스트 교정"""
        if not self.lm or language not in self.lm:
            return segments

        model = self.lm[language]
        corrected_segments = []

        for segment in segments:
            text = segment.text.strip()
            if not text:
                corrected_segments.append(segment)
                continue

            # 단어 후보 생성 및 평가 (간단한 예시)
            words = text.split()
            corrected_words = []

            for i, word in enumerate(words):
                # 간단한 교정: 주변 단어 컨텍스트 활용
                context_before = " ".join(words[max(0, i - 2):i])
                context_after = " ".join(words[i + 1:min(len(words), i + 3)])

                # 교정 후보 생성 (예: 비슷한 단어들)
                candidates = [word] + self._generate_candidates(word, language)

                # 언어 모델로 가장 적합한 후보 선택
                best_candidate = word
                best_score = float('-inf')

                for candidate in candidates:
                    test_text = f"{context_before} {candidate} {context_after}".strip()
                    score = model.score(test_text)

                    if score > best_score:
                        best_score = score
                        best_candidate = candidate

                corrected_words.append(best_candidate)

            # 교정된 텍스트
            corrected_text = " ".join(corrected_words)

            # 원본과 교정본이 같으면 원본 사용
            if corrected_text == text:
                corrected_segments.append(segment)
            else:
                # 수정된 경우만 로그
                self.logger.info(f"텍스트 교정: '{text}' → '{corrected_text}'")
                corrected_segments.append(STTResults(
                    text=corrected_text,
                    start=segment.start,
                    end=segment.end
                ))

        return corrected_segments

    def _generate_candidates(self, word, language):
        """단어 교정 후보 생성 (언어별 특화)"""
        # 간단한 예시 구현 (실제로는 더 정교한 알고리즘 필요)
        candidates = []

        # 한국어 특화 교정
        if language == "ko":
            # 자음/모음 오류 교정
            if word.endswith("데"):
                candidates.append(word[:-1] + "네")
            if word.endswith("던"):
                candidates.append(word[:-1] + "든")
            # 숫자 표현 정규화
            if "삼십" in word:
                candidates.append(word.replace("삼십", "30"))

        # 영어 특화 교정
        elif language == "en":
            # 일반적인 오류 교정
            if word == "i":
                candidates.append("I")
            if word == "cant":
                candidates.append("can't")

        return candidates[:3]  # 최대 3개 후보

    def _context_based_postprocessing(self, segments):
        """컨텍스트 기반 후처리"""
        if not segments:
            return segments

        processed = []

        # 연속된 세그먼트 통합 (필요한 경우)
        i = 0
        while i < len(segments):
            current = segments[i]

            # 다음 세그먼트가 있고, 매우 짧으며, 텍스트가 불완전해 보이는 경우
            if (i + 1 < len(segments) and
                    segments[i + 1].end - segments[i + 1].start < 1.0 and
                    not any(segments[i + 1].text.endswith(c) for c in ['.', '?', '!', '。', '？', '！'])):

                next_seg = segments[i + 1]
                merged = STTResults(
                    text=current.text + " " + next_seg.text,
                    start=current.start,
                    end=next_seg.end
                )
                processed.append(merged)
                i += 2  # 두 세그먼트 건너뛰기
            else:
                processed.append(current)
                i += 1

        # 숫자, 날짜, 시간 등 정규화
        normalized = []
        for segment in processed:
            normalized_text = self._normalize_special_entities(segment.text)
            normalized.append(STTResults(
                text=normalized_text,
                start=segment.start,
                end=segment.end
            ))

        return normalized

    def _normalize_special_entities(self, text):
        """특수 엔티티 정규화 (숫자, 날짜, 시간 등)"""
        import re

        # 숫자 표현 정규화
        def repl_numbers(match):
            word = match.group(0)

            # 한글 숫자를 아라비아 숫자로 변환
            number_map = {
                '일': '1', '이': '2', '삼': '3', '사': '4', '오': '5',
                '육': '6', '칠': '7', '팔': '8', '구': '9', '십': '10',
                '백': '100', '천': '1000', '만': '10000', '억': '100000000'
            }

            # 간단한 예시 변환 (실제로는 더 복잡한 로직 필요)
            for k, v in number_map.items():
                if k in word:
                    return word.replace(k, v)
            return word

        # 숫자 패턴 찾기 (간단한 예시)
        text = re.sub(r'[일이삼사오육칠팔구십백천만억]+(?=\s|$)', repl_numbers, text)

        # 시간 표현 정규화
        text = re.sub(r'(\d+)\s*시\s*(\d+)\s*분', r'\1:\2', text)

        # 전화번호 포맷팅
        text = re.sub(r'(\d{2,3})\s*(\d{3,4})\s*(\d{4})', r'\1-\2-\3', text)

        return text

    def _apply_contextual_correction(self, text, language):
        """컨텍스트 기반 텍스트 교정 (LLM 활용 가능)"""
        # 실제 구현에서는 LLM API 호출 또는 로컬 모델 사용
        # 여기서는 간단한 규칙 기반 교정만 시뮬레이션

        import re

        # 1. 문장 시작 대문자화 (영어)
        if language == "en":
            text = re.sub(r'(?<=[\.\?\!]\s)([a-z])', lambda m: m.group(1).upper(), text)
            text = re.sub(r'^([a-z])', lambda m: m.group(1).upper(), text)

        # 2. 반복되는 단어 제거
        text = re.sub(r'\b(\w+)(\s+\1\b)+', r'\1', text)

        # 3. 잘못된 문장 부호 교정
        text = re.sub(r'([^\s])\.([^\s])', r'\1. \2', text)  # 온점 후 공백
        text = re.sub(r'\s+([\.,:;?!])', r'\1', text)  # 문장 부호 앞 공백 제거

        # 4. 불필요한 간투사 제거 (um, uh, 어, 음 등)
        if language == "en":
            text = re.sub(r'\b(um|uh|like)\b\s*', '', text)
        elif language == "ko":
            text = re.sub(r'\b(어|음|그)\b\s*', '', text)

        return text.strip()

    def _redistribute_text(self, corrected_text, original_segments):
        """교정된 텍스트를 원래 세그먼트에 재분배"""
        # 간단한 구현: 원래 세그
