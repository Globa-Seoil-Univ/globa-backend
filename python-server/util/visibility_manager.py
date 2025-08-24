import threading
import time
from datetime import datetime
from threading import Lock
from botocore.exceptions import ClientError

class VisibilityTimeoutManager:
    """
        SQS Timeout 관리 파일
    """

    def __init__(self, sqs_client, queue_url, logger,
                 visibility_timeout_seconds = 1200, # 동적 변경을 위한 최초 주기 ( AWS GUI 환경의 디폴트값 )
                 extension_threshold_seconds = 300, # 미만 조건
                 monitor_interval=60 ): # 모니터링 주기
        self.sqs = sqs_client
        self.queue_url = queue_url
        self.logger = logger

        self.visibility_timeout_seconds = visibility_timeout_seconds
        self.extension_threshold_seconds = extension_threshold_seconds
        self.monitor_interval = monitor_interval

        self.active_messages = {} # 활성 메시지 추적용임. e.g. {receipt_handle : {'start_time': datetime, 'last_extended':datetime}}
        self.message_lock = Lock() # 마찬가지로 추적용

        self.monitor_thread = None
        self.is_running = False

    def start_monitoring(self):
        """
            모니터링 시작
        """
        if self.monitor_thread and self.monitor_thread.is_alive():
            return

        self.is_running = True
        self.monitor_thread = threading.Thread(target=self._monitor_loop, daemon=True)
        self.monitor_thread.start()
        self.logger.info("모니터링 시작")

    def stop_monitoring(self):
        """
            모니터링 종료
        """
        self.is_running = False
        if self.monitor_thread :
            self.monitor_thread.join(timeout=5)
        self.logger.info("모니터링 종료")

    def register_message(self, receipt_handle):
        """메시지 처리 시작 시 등록"""
        with self.message_lock:
            self.active_messages[receipt_handle] = {
                'start_time': datetime.now(),
                'last_extended': datetime.now()
            }
        self.logger.debug(f"메시지 처리 시작 등록: {receipt_handle[:20]}...")

    def unregister_message(self, receipt_handle):
        """메시지 처리 완료 시 등록 해제"""
        with self.message_lock:
            removed = self.active_messages.pop(receipt_handle, None)

        if removed:
            total_time = (datetime.now() - removed['start_time']).total_seconds()
            self.logger.info(f"메시지 처리 완료 - 총 처리시간: {total_time:.1f}초, Receipt: {receipt_handle[:20]}...")

    def get_active_message_count(self):
        """현재 처리 중인 메시지 수 반환"""
        with self.message_lock:
            return len(self.active_messages)

    def _monitor_loop(self):
        """모니터링 메인 루프"""
        while self.is_running:
            try:
                self._check_and_extend_messages()
            except Exception as e:
                self.logger.error(f"VisibilityTimeout 모니터링 중 에러: {e}")

            time.sleep(self.monitor_interval)

    def _check_and_extend_messages(self):
        """활성 메시지들의 VisibilityTimeout을 확인하고 필요시 갱신"""
        current_time = datetime.now()
        messages_to_extend = []

        with self.message_lock:
            for receipt_handle, info in self.active_messages.items():
                # 마지막 갱신 시간으로부터 경과 시간 계산
                last_extended = info.get('last_extended', info['start_time'])
                elapsed_since_last_extension = (current_time - last_extended).total_seconds()

                # VisibilityTimeout이 임계값 미만 남았으면 갱신 대상에 추가
                remaining_time = self.visibility_timeout_seconds - elapsed_since_last_extension

                if remaining_time < self.extension_threshold_seconds:
                    messages_to_extend.append({
                        'receipt_handle': receipt_handle,
                        'remaining_time': remaining_time,
                        'elapsed_total': (current_time - info['start_time']).total_seconds()
                    })

        # VisibilityTimeout 갱신 실행
        extended_count = 0
        for msg_info in messages_to_extend:
            if self._extend_visibility_timeout(msg_info):
                extended_count += 1

        if extended_count > 0:
            self.logger.info(f"VisibilityTimeout 갱신 완료: {extended_count}개 메시지")

    def _extend_visibility_timeout(self, msg_info):
        """특정 메시지의 VisibilityTimeout을 갱신"""
        receipt_handle = msg_info['receipt_handle']

        try:
            self.sqs.change_message_visibility(
                QueueUrl=self.queue_url,
                ReceiptHandle=receipt_handle,
                VisibilityTimeout=self.visibility_timeout_seconds
            )

            # 갱신 시간 업데이트
            with self.message_lock:
                if receipt_handle in self.active_messages:
                    self.active_messages[receipt_handle]['last_extended'] = datetime.now()

            self.logger.info(
                f"VisibilityTimeout 갱신 성공 - "
                f"남은시간: {msg_info['remaining_time']:.1f}초, "
                f"총 처리시간: {msg_info['elapsed_total']:.1f}초, "
                f"Receipt: {receipt_handle[:20]}..."
            )
            return True

        except ClientError as e:
            error_code = e.response['Error']['Code']
            if error_code == 'ReceiptHandleIsInvalid':
                # 메시지가 이미 삭제되었거나 만료됨
                self.logger.info(f"메시지가 이미 처리됨 또는 만료됨: {receipt_handle[:20]}...")
                with self.message_lock:
                    self.active_messages.pop(receipt_handle, None)
            else:
                self.logger.error(f"VisibilityTimeout 갱신 실패: {e}")
            return False

        except Exception as e:
            self.logger.error(f"VisibilityTimeout 갱신 중 예외 발생: {e}")
            return False