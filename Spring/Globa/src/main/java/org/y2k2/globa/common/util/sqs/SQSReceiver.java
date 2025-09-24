package org.y2k2.globa.common.util.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.sqs.dto.response.MessageTrackingInfo;
import org.y2k2.globa.application.sqs.dto.response.ResponseDLQDto;
import org.y2k2.globa.application.sqs.dto.response.ResponseSQSDto;
import org.y2k2.globa.application.sqs.service.DLQService;
import org.y2k2.globa.application.sqs.service.SQSService;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SQSReceiver {
    @Value("${spring.cloud.aws.sqs.receive-queue-name}")
    private String receiveQueueName;
    @Value("${spring.cloud.aws.sqs.dlq-queue-name}")
    private String dlqQueueName;

    private String receiveQueueUrl;
    private String dlqQueueUrl;
    private int visibilityTimeout;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    private final SQSService sqsService;
    private final DLQService dlqService;

    // 스레드 개수
    private final ScheduledExecutorService sharedScheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, MessageTrackingInfo> messageTracker = new ConcurrentHashMap<>();

    // 가시성 모니터링 주기 (초 단위)
    private static final int VISIBILITY_CHECK_INTERVAL = 2;

    // 가시성 타임아웃 연장 시간 (초 단위)
    private static final int VISIBILITY_EXTENSION = 60;

    // 가시성 타임아웃 연장 최대 횟수
    private static final int MAX_EXTEND_COUNT = 3;

    // 가시성 타임아웃 연장 시점 (초 단위) -> 메시지 수신 후 5초가 남은 경우
    private static final int VISIBILITY_THRESHOLD = 5;

    // 기본 가시성 타임아웃 (초 단위) -> AWS SQS의 기본 가시성 타임아웃은 30초
    private static final int DEFAULT_VISIBILITY_TIMEOUT = 30;

    // 백오프 지연 시간 (1차 재시도: 20초, 2차 재시도: 40초)
    private static final int[] BACKOFF_DELAYS = {20, 40};

    @PostConstruct
    public void init() {
        // SQS 큐 URL 가져오기
        this.receiveQueueUrl = sqsClient.getQueueUrl(
                GetQueueUrlRequest.builder()
                        .queueName(receiveQueueName)
                        .build()
        ).queueUrl();

        this.dlqQueueUrl = sqsClient.getQueueUrl(
                GetQueueUrlRequest.builder()
                        .queueName(dlqQueueName)
                        .build()
        ).queueUrl();

        GetQueueAttributesRequest getAttributesRequest = GetQueueAttributesRequest.builder()
                .queueUrl(receiveQueueUrl)
                .attributeNames(QueueAttributeName.VISIBILITY_TIMEOUT)
                .build();

        GetQueueAttributesResponse attributesResponse = sqsClient.getQueueAttributes(getAttributesRequest);
        this.visibilityTimeout = attributesResponse.attributes().get(QueueAttributeName.VISIBILITY_TIMEOUT) != null
                ? Integer.parseInt(attributesResponse.attributes().get(QueueAttributeName.VISIBILITY_TIMEOUT))
                : DEFAULT_VISIBILITY_TIMEOUT;

        log.info("SQS queue URL initialized = {}, {}", receiveQueueUrl, dlqQueueUrl);
    }

    @SqsListener(
            value = "${spring.cloud.aws.sqs.receive-queue-name}",
            pollTimeoutSeconds = "20"
    )
    public void receiveMessage(
            Message message
    ) {
        // 재시도 횟수 확인
        int receiveCount = message.attributes().get(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT) != null
                ? Integer.parseInt(message.attributes().get(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT))
                : 1;
        String receiptHandle = message.receiptHandle();
        String messageId = message.messageId();

        Instant receiveTime = Instant.now();
        MessageTrackingInfo trackingInfo = new MessageTrackingInfo(
                messageId,
                receiptHandle,
                receiveTime,
                visibilityTimeout,
                0
        );

        messageTracker.put(messageId, trackingInfo);
        ScheduledFuture<?> visibilityMonitor = startVisibilityMonitoring(messageId);
        scheduledTasks.put(messageId, visibilityMonitor);

        try {
            log.info("Received SQS message with ID = {}, receiveCount = {}", messageId, receiveCount);

            // 재시도 횟수에 따라 백오프 지연 적용
            if (receiveCount > 1) {
                int delaySeconds = BACKOFF_DELAYS[Math.min(receiveCount - 2, BACKOFF_DELAYS.length - 1)];
                log.warn("Message received {} times, applying backoff delay of {} seconds for messageId = {}",
                        receiveCount, delaySeconds, messageId);

                try {
                    Thread.sleep(delaySeconds * 1000);
                } catch (InterruptedException e) {
                    log.warn("Backoff interrupted for messageId = {}, error = {}", messageId, e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

            ResponseSQSDto response = objectMapper.readValue(message.body(), ResponseSQSDto.class);
            log.info("Received SQS message = {}, messageId = {}, receiveCount = {}", response, messageId, receiveCount);

            Long recordId = response.recordId();
            String encryptedUserId = response.userId();

            if (response.status().equalsIgnoreCase("success")
                    && recordId > 0
                    && !encryptedUserId.isEmpty()) {
                log.info("Processing successful response for recordId = {}", recordId);
                sqsService.success(response);
            } else {
                log.warn("Processing failed response for recordId = {}", recordId);
                sqsService.failed(response);
            }

            // 완료 처리
            deleteMessage(receiveQueueUrl, receiptHandle);
        } catch (JsonProcessingException e) {
            log.error("Failed to process SQS message = {}", e.getMessage());
            deleteMessage(receiveQueueUrl, receiptHandle);
        } catch (Exception e) {
            log.error("Unexpected error while processing messageId = {}, error = {}", messageId, e.getMessage());
            deleteMessage(receiveQueueUrl, receiptHandle);
        } finally {
            cleanup(messageId);
        }
    }

    @SqsListener(
            value = "${spring.cloud.aws.sqs.dlq-queue-name}",
            pollTimeoutSeconds = "20"
    )
    public void handleDLQ(
            Message message
    ) {
        String messageId = message.messageId();
        String receiptHandle = message.receiptHandle();
        String body = message.body();

        try {
            log.info("Received DLQ message with ID = {}", messageId);

            ResponseDLQDto response = objectMapper.readValue(body, ResponseDLQDto.class);
            dlqService.process(response);
        } catch (JsonProcessingException e) {
            log.error("Failed to process DLQ message = {}", e.getMessage());
        } finally {
            deleteMessage(dlqQueueUrl, receiptHandle);
        }
    }

    private void deleteMessage(String queueUrl, String receiptHandle) {
        sqsClient.deleteMessage(
                DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(receiptHandle)
                        .build()
        );
        log.info("Message deleted with receiptHandle = {}", receiptHandle);
    }

    private void cleanup(String messageId) {
        // 현재 실행 중인 스케줄된 태스크를 취소
        ScheduledFuture<?> scheduledTask = scheduledTasks.get(messageId);
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }

        // 메시지 추적 정보 제거
        scheduledTasks.remove(messageId);
        messageTracker.remove(messageId);

        log.info("Cleanup completed for messageId = {}", messageId);
    }

    private ScheduledFuture<?> startVisibilityMonitoring(String messageId) {
        // 특정 시간마다 작업을 반복 실행
        ScheduledFuture<?> scheduledTask = sharedScheduler.scheduleAtFixedRate(() -> {
            log.info("Running...");

            try {
                if (!messageTracker.containsKey(messageId)) {
                    // 메시지 추적 정보가 없으면 모니터링 중지
                    ScheduledFuture<?> task = scheduledTasks.remove(messageId);
                    if (task != null && !task.isCancelled()) {
                        task.cancel(false);
                    }

                    return;
                }

                if (shouldExtendVisibility(messageId)) {
                    extendVisibilityTimeout(messageId);
                }
            } catch (Exception e) {
                log.error("Error during visibility monitoring for messageId = {}, error = {}", messageId, e.getMessage());
                cleanup(messageId);
            }
        }, VISIBILITY_CHECK_INTERVAL, VISIBILITY_CHECK_INTERVAL, TimeUnit.SECONDS);

        log.info("Started visibility monitoring for messageId = {}", messageId);
        return scheduledTask;
    }

    private boolean shouldExtendVisibility(String messageId) {
        MessageTrackingInfo trackingInfo = messageTracker.get(messageId);

        if (trackingInfo == null) {
            log.warn("No tracking info found for messageId = {}", messageId);
            return false;
        }

        // 최대 재시도 횟수 초과 여부 확인
        if (trackingInfo.extendCount() >= MAX_EXTEND_COUNT) {
            log.error("Message {} exceeded maximum visibility extensions, it will be reprocessed", messageId);
            cleanup(messageId);

            return false;
        }

        Instant now = Instant.now();
        long elapsedSeconds = Duration.between(trackingInfo.receivedAt(), now).getSeconds();
        long remainingSeconds = trackingInfo.visibilityTimeout() - elapsedSeconds;

        return remainingSeconds <= VISIBILITY_THRESHOLD;
    }

    private void extendVisibilityTimeout(String messageId) {
        MessageTrackingInfo trackingInfo = messageTracker.get(messageId);

        if (trackingInfo == null) {
            log.warn("No tracking info found for messageId = {}", messageId);
            return;
        }

        log.info("Extending visibility timeout for messageId = {}", messageId);
        
        ChangeMessageVisibilityRequest request = ChangeMessageVisibilityRequest.builder()
                .queueUrl(receiveQueueUrl)
                .receiptHandle(trackingInfo.receiptHandle())
                .visibilityTimeout(VISIBILITY_EXTENSION)
                .build();

        // 타임아웃 연장
        sqsClient.changeMessageVisibility(request);

        // 추적 정보 업데이트
        trackingInfo = new MessageTrackingInfo(
                trackingInfo.messageId(),
                trackingInfo.receiptHandle(),
                Instant.now(),
                VISIBILITY_EXTENSION,
                trackingInfo.extendCount() + 1
        );

        messageTracker.put(messageId, trackingInfo);
    }
}
