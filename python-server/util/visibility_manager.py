import os
import threading
import time
from datetime import datetime
from threading import Lock
from botocore.exceptions import ClientError


class VisibilityTimeoutManager:
    """
        SQS Timeout 관리 파일 (점진적 증가 기능 추가)
    """

    def __init__(self, sqs_client, queue_url, logger,
                 visibility_timeout_seconds=int(os.environ.get('VISIBILITY_TIMEOUT_SECONDS', 300)),  # 기본 5분
                 extension_threshold_seconds=int(os.environ.get('EXTENSION_THRESHOLD_SECONDS', 60)),  # 1분 미만
                 monitor_interval=int(os.environ.get('MONITOR_INTERVAL_SECONDS', 60)),  # 1분 주기
                 enable_progressive_timeout=bool(
                     os.environ.get('ENABLE_PROGRESSIVE_TIMEOUT', 'true').lower() == 'true'),
                 max_extension_count=int(os.environ.get('MAX_EXTENSION_COUNT', 4)),
                 timeout_multiplier=float(os.environ.get('TIMEOUT_MULTIPLIER', 2.0)),
                 max_timeout_seconds=int(os.environ.get('MAX_TIMEOUT_SECONDS', 4800))):

        self.sqs = sqs_client
        self.queue_url = queue_url
        self.logger = logger

        self.visibility_timeout_seconds = visibility_timeout_seconds
        self.extension_threshold_seconds = extension_threshold_seconds
        self.monitor_interval = monitor_interval


        self.enable_progressive_timeout = enable_progressive_timeout
        self.max_extension_count = max_extension_count
        self.timeout_multiplier = timeout_multiplier
        self.max_timeout_seconds = max_timeout_seconds

        # 활성 메시지 추적 (확장된 정보)
        self.active_messages = {}  # {receipt_handle : {'start_time': datetime, 'last_extended':datetime, 'message_id': str, 'extension_count': int, 'current_timeout': int}}
        self.message_lock = Lock()

        self.monitor_thread = None
        self.is_running = False

        self.logger.info(f"🔧 VisibilityTimeout 설정:")
        self.logger.info(f"   📊 기본 타임아웃: {self._format_duration(self.visibility_timeout_seconds)}")
        self.logger.info(f"   📊 점진적 증가: {'활성화' if self.enable_progressive_timeout else '비활성화'}")
        if self.enable_progressive_timeout:
            self.logger.info(f"   📊 증가 배수: {self.timeout_multiplier}배")
            self.logger.info(f"   📊 최대 갱신 횟수: {self.max_extension_count}회")
            self.logger.info(f"   📊 최대 타임아웃: {self._format_duration(self.max_timeout_seconds)}")

    def start_monitoring(self):
        """모니터링 시작"""
        if self.monitor_thread and self.monitor_thread.is_alive():
            return

        self.is_running = True
        self.monitor_thread = threading.Thread(target=self._monitor_loop, daemon=True)
        self.monitor_thread.start()
        self.logger.info("📊 VisibilityTimeout 모니터링 시작")

    def stop_monitoring(self):
        """모니터링 종료"""
        self.is_running = False
        if self.monitor_thread:
            self.monitor_thread.join(timeout=5)
        self.logger.info("🛑 VisibilityTimeout 모니터링 종료")

    def register_message(self, receipt_handle, message_id=None):
        """메시지 처리 시작 시 등록"""
        with self.message_lock:
            self.active_messages[receipt_handle] = {
                'start_time': datetime.now(),
                'last_extended': datetime.now(),
                'message_id': message_id or receipt_handle[:8],
                'extension_count': 0,  # 🆕 갱신 횟수
                'current_timeout': self.visibility_timeout_seconds  # 🆕 현재 타임아웃
            }

        message_display_id = message_id or receipt_handle[:8]
        self.logger.info(f"🔄 메시지 처리 시작 등록 - ID: {message_display_id}")

    def unregister_message(self, receipt_handle):
        """메시지 처리 완료 시 등록 해제 (안전 장치 추가)"""
        with self.message_lock:
            removed = self.active_messages.pop(receipt_handle, None)

        if removed:
            total_time = (datetime.now() - removed['start_time']).total_seconds()
            message_id = removed.get('message_id', receipt_handle[:8])
            extension_count = removed.get('extension_count', 0)

            if extension_count > 0:
                self.logger.info(
                    f"✅ 메시지 모니터링 종료 - ID: {message_id}, 총 처리시간: {self._format_duration(total_time)}, 갱신횟수: {extension_count}회")
            else:
                self.logger.info(f"✅ 메시지 모니터링 종료 - ID: {message_id}, 총 처리시간: {self._format_duration(total_time)}")
        else:
            self.logger.warning(f"이미 제거된 메시지 unregister 시도 - Handle: {receipt_handle[:8]}")

    def get_active_message_count(self):
        """현재 처리 중인 메시지 수 반환"""
        with self.message_lock:
            return len(self.active_messages)

    def _calculate_next_timeout(self, current_extension_count):
        """다음 갱신 시 사용할 타임아웃 계산"""
        if not self.enable_progressive_timeout:
            return self.visibility_timeout_seconds

        # 갱신 횟수에 따른 점진적 증가
        next_timeout = int(self.visibility_timeout_seconds * (self.timeout_multiplier ** current_extension_count))

        # 최대 타임아웃 제한
        next_timeout = min(next_timeout, self.max_timeout_seconds)

        return next_timeout

    def _monitor_loop(self):
        """모니터링 메인 루프"""
        while self.is_running:
            try:
                self._print_monitoring_status()
                self._check_and_extend_messages()
            except Exception as e:
                self.logger.error(f"❌ VisibilityTimeout 모니터링 중 에러: {e}")

            time.sleep(self.monitor_interval)

    def _print_monitoring_status(self):
        """현재 모니터링 상태를 상세히 출력"""
        current_time = datetime.now()

        with self.message_lock:
            active_count = len(self.active_messages)

            if active_count == 0:
                self.logger.info("📊 [모니터링 상태] 현재 처리 중인 메시지 없음")
                return

            self.logger.info(f"📊 [모니터링 상태] 현재 처리 중인 메시지: {active_count}개")
            self.logger.info("=" * 90)

            for receipt_handle, info in self.active_messages.items():
                message_id = info.get('message_id', receipt_handle[:8])
                start_time = info['start_time']
                last_extended = info.get('last_extended', start_time)
                extension_count = info.get('extension_count', 0)
                current_timeout = info.get('current_timeout', self.visibility_timeout_seconds)

                # 시간 계산
                total_elapsed = (current_time - start_time).total_seconds()
                elapsed_since_extension = (current_time - last_extended).total_seconds()
                remaining_time = current_timeout - elapsed_since_extension

                # 다음 갱신 시 타임아웃 계산
                next_timeout = self._calculate_next_timeout(extension_count)

                # 상태 판단
                if remaining_time < self.extension_threshold_seconds:
                    if extension_count >= self.max_extension_count:
                        status = "🔴 갱신 한계 도달"
                    else:
                        status = "🔴 갱신 필요"
                elif remaining_time < self.extension_threshold_seconds * 2:
                    status = "🟡 주의"
                else:
                    status = "🟢 정상"

                # 시간 포맷팅
                total_time_str = self._format_duration(total_elapsed)
                remaining_time_str = self._format_duration(remaining_time)
                current_timeout_str = self._format_duration(current_timeout)
                next_timeout_str = self._format_duration(next_timeout)

                self.logger.info(
                    f"  📋 메시지 ID: {message_id} | {status}\n"
                    f"     ⏱️  총 처리시간: {total_time_str}\n"
                    f"     ⏳ 남은 시간: {remaining_time_str}\n"
                    f"     🔧 현재 타임아웃: {current_timeout_str}\n"
                    f"     📈 갱신 횟수: {extension_count}회 (최대: {self.max_extension_count}회)\n"
                    f"     🎯 다음 타임아웃: {next_timeout_str}\n"
                    f"     📝 마지막 갱신: {last_extended.strftime('%H:%M:%S')}"
                )

            self.logger.info("=" * 90)

    def _format_duration(self, seconds):
        """초를 읽기 쉬운 형태로 포맷팅"""
        if seconds < 0:
            return f"-{self._format_duration(-seconds)}"

        hours = int(seconds // 3600)
        minutes = int((seconds % 3600) // 60)
        secs = int(seconds % 60)

        if hours > 0:
            return f"{hours}시간 {minutes}분 {secs}초"
        elif minutes > 0:
            return f"{minutes}분 {secs}초"
        else:
            return f"{secs}초"

    def _check_and_extend_messages(self):
        """활성 메시지들의 VisibilityTimeout을 확인하고 필요시 갱신"""
        current_time = datetime.now()
        messages_to_extend = []

        with self.message_lock:
            for receipt_handle, info in self.active_messages.items():
                last_extended = info.get('last_extended', info['start_time'])
                current_timeout = info.get('current_timeout', self.visibility_timeout_seconds)
                extension_count = info.get('extension_count', 0)

                elapsed_since_last_extension = (current_time - last_extended).total_seconds()
                remaining_time = current_timeout - elapsed_since_last_extension

                # 갱신이 필요하고 최대 갱신 횟수를 초과하지 않은 경우
                if (remaining_time < self.extension_threshold_seconds and
                        extension_count < self.max_extension_count):
                    next_timeout = self._calculate_next_timeout(extension_count)

                    messages_to_extend.append({
                        'receipt_handle': receipt_handle,
                        'message_id': info.get('message_id', receipt_handle[:8]),
                        'remaining_time': remaining_time,
                        'elapsed_total': (current_time - info['start_time']).total_seconds(),
                        'extension_count': extension_count,
                        'current_timeout': current_timeout,
                        'next_timeout': next_timeout
                    })

        # VisibilityTimeout 갱신 실행
        if messages_to_extend:
            self.logger.info(f"🔧 VisibilityTimeout 갱신 시작 - 대상: {len(messages_to_extend)}개 메시지")

            extended_count = 0
            failed_count = 0
            limit_reached_count = 0

            for msg_info in messages_to_extend:
                result = self._extend_visibility_timeout(msg_info)
                if result == 'success':
                    extended_count += 1
                elif result == 'limit_reached':
                    limit_reached_count += 1
                else:
                    failed_count += 1

            # 갱신 결과 요약
            if extended_count > 0:
                self.logger.info(f"✅ VisibilityTimeout 갱신 완료: {extended_count}개 성공")
            if limit_reached_count > 0:
                self.logger.warning(f"⚠️ 갱신 한계 도달: {limit_reached_count}개 메시지")
            if failed_count > 0:
                self.logger.warning(f"❌ VisibilityTimeout 갱신 실패: {failed_count}개")

    def _extend_visibility_timeout(self, msg_info):
        """🆕 특정 메시지의 VisibilityTimeout을 점진적으로 갱신"""
        receipt_handle = msg_info['receipt_handle']
        message_id = msg_info['message_id']
        extension_count = msg_info['extension_count']
        next_timeout = msg_info['next_timeout']

        with self.message_lock:
            if receipt_handle not in self.active_messages:
                self.logger.warning(f"⚠이미 완료된 메시지 갱신 시도 무시 - ID: {message_id}")
                return 'already_completed'

        # 최대 갱신 횟수 체크
        if extension_count >= self.max_extension_count:
            self.logger.warning(f"⚠️ 최대 갱신 횟수 도달 - ID: {message_id} (갱신 {extension_count}회)")
            return 'limit_reached'

        try:
            self.logger.info(f"🔧 VisibilityTimeout 갱신 시도 - ID: {message_id} ({extension_count + 1}회차)")

            self.sqs.change_message_visibility(
                QueueUrl=self.queue_url,
                ReceiptHandle=receipt_handle,
                VisibilityTimeout=next_timeout
            )

            # 갱신 정보 업데이트
            with self.message_lock:
                if receipt_handle in self.active_messages:
                    self.active_messages[receipt_handle]['last_extended'] = datetime.now()
                    self.active_messages[receipt_handle]['extension_count'] = extension_count + 1
                    self.active_messages[receipt_handle]['current_timeout'] = next_timeout

            # 상세 로그
            remaining_time_str = self._format_duration(msg_info['remaining_time'])
            total_time_str = self._format_duration(msg_info['elapsed_total'])
            current_timeout_str = self._format_duration(msg_info['current_timeout'])
            next_timeout_str = self._format_duration(next_timeout)

            self.logger.info(
                f"✅ VisibilityTimeout 갱신 성공 - ID: {message_id}\n"
                f"   📊 갱신 횟수: {extension_count} → {extension_count + 1}회\n"
                f"   📊 갱신 전 남은시간: {remaining_time_str}\n"
                f"   📊 총 처리시간: {total_time_str}\n"
                f"   📊 이전 타임아웃: {current_timeout_str}\n"
                f"   📊 새로운 타임아웃: {next_timeout_str} ({'점진적 증가' if self.enable_progressive_timeout else '고정값'})"
            )
            return 'success'

        except ClientError as e:
            error_code = e.response['Error']['Code']
            error_message = e.response['Error']['Message']

            if error_code == 'ReceiptHandleIsInvalid' or 'Message does not exist' in error_message:
                self.logger.warning(f"⚠️ 메시지가 이미 처리됨 또는 만료됨 - ID: {message_id}")
                with self.message_lock:
                    self.active_messages.pop(receipt_handle, None)
            else:
                self.logger.error(f"❌ VisibilityTimeout 갱신 실패 - ID: {message_id}, 에러: {error_code} - {error_message}")
            return 'failed'

        except Exception as e:
            self.logger.error(f"❌ VisibilityTimeout 갱신 중 예외 발생 - ID: {message_id}, 에러: {str(e)}")
            return 'failed'