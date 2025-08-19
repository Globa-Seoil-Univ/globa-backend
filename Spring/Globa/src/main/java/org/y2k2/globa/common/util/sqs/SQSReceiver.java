package org.y2k2.globa.common.util.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.sqs.service.SQSService;
import org.y2k2.globa.application.sqs.dto.response.MessageTrackingInfo;
import org.y2k2.globa.application.sqs.dto.response.ResponseSQSDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SQSReceiver {
    @Value("${spring.cloud.aws.sqs.dlq-name}")
    private String dlqQueueName;

    private final SqsClient sqsClient;
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;
    private final SQSService sqsService;

    private final Map<String, MessageTrackingInfo> messageTracker = new ConcurrentHashMap<>();

    // 가시성 모니터링 주기 (초 단위)
    private static final int VISIBILITY_CHECK_INTERVAL = 5;

    // 가시성 타임아웃 연장 시간 (초 단위)
    private static final int VISIBILITY_EXTENSION = 60;

    // 가시성 타임아웃 연장 최대 횟수
    private static final int MAX_EXTEND_COUNT = 3;

    // 가시성 타임아웃 연장 시점 (초 단위) -> 메시지 수신 후 20초가 남은 경우
    private static final int VISIBILITY_THRESHOLD = 20;

    // 기본 가시성 타임아웃 (초 단위) -> AWS SQS의 기본 가시성 타임아웃은 30초
    private static final int DEFAULT_VISIBILITY_TIMEOUT = 30;

    // 백오프 지연 시간 (1차 재시도: 20초, 2차 재시도: 40초)
    private static final int[] BACKOFF_DELAYS = {20, 40};

    @SqsListener("globa-to-spring.fifo")
    public void receiveMessage(
            Message message
    ) {
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
                DEFAULT_VISIBILITY_TIMEOUT,
                0
        );

        messageTracker.put(messageId, trackingInfo);
        ScheduledExecutorService visibilityMonitor = startVisibilityMonitoring(messageId);

        try {
            if (receiveCount > 1) {
                int delaySeconds = BACKOFF_DELAYS[Math.min(receiveCount - 2, BACKOFF_DELAYS.length - 1)];
                log.warn("Message received {} times, applying backoff delay of {} seconds for messageId = {}",
                        receiveCount, delaySeconds, messageId);

                Thread.sleep(delaySeconds * 1000);
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

            // 완료 처리 (삭제)
            sqsClient.deleteMessage(
                    DeleteMessageRequest.builder()
                            .queueUrl(messageId)
                            .receiptHandle(receiptHandle)
                            .build()
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to process SQS message = {}", e.getMessage());

            sqsTemplate.send(
                dlqQueueName,
                message
            );
        } catch (InterruptedException e) {
            log.error("Thread interrupted during backoff delay for messageId = {}, error = {}", messageId, e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected error while processing messageId = {}, error = {}", messageId, e.getMessage());

            // Send to DLQ
            sqsTemplate.send(
                dlqQueueName,
                message.body()
            );
        } finally {
            messageTracker.remove(messageId);

            if (!visibilityMonitor.isShutdown()) {
                visibilityMonitor.shutdown();
            }
        }
    }

    private ScheduledExecutorService startVisibilityMonitoring(String messageId) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "visibility-monitor-" + messageId);
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!messageTracker.containsKey(messageId)) {
                    scheduler.shutdown();
                    return;
                }

                extendVisibilityTimeoutIfNeeded(messageId);
            } catch (CustomException e) {
                scheduler.shutdown();
            } catch (Exception e) {
                log.error("Error during visibility monitoring for messageId = {}, error = {}", messageId, e.getMessage());
                scheduler.shutdown();
            }
        }, VISIBILITY_CHECK_INTERVAL, VISIBILITY_CHECK_INTERVAL, TimeUnit.SECONDS);

        log.debug("Started visibility monitoring for messageId = {}", messageId);
        return scheduler;
    }

    private void extendVisibilityTimeoutIfNeeded(String messageId) {
        GetQueueUrlResponse queueUrlResponse = sqsClient.getQueueUrl(
                GetQueueUrlRequest.builder()
                        .queueName("globa-to-spring.fifo")
                        .build()
        );
        String queueUrl = queueUrlResponse.queueUrl();

        GetQueueAttributesRequest getAttributesRequest = GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.VISIBILITY_TIMEOUT)
                .build();

        GetQueueAttributesResponse attributesResponse = sqsClient.getQueueAttributes(getAttributesRequest);
        int visibilityTimeout = Integer.parseInt(
                attributesResponse.attributes().get(QueueAttributeName.VISIBILITY_TIMEOUT)
        );

        log.debug("Current visibility timeout for messageId = {}: {} seconds", messageId, visibilityTimeout);

        if (shouldExtendVisibility(messageId)) {
            extendVisibilityTimeout(queueUrl, messageId);
        }
    }

    private boolean shouldExtendVisibility(String messageId) {
        MessageTrackingInfo trackingInfo = messageTracker.get(messageId);

        Instant now = Instant.now();
        long elapsedSeconds = Duration.between(trackingInfo.receivedAt(), now).getSeconds();
        long remainingSeconds = trackingInfo.visibilityTimeout() - elapsedSeconds;

        // 최대 재시도 및 남은 시간이 없는 경우
        if (remainingSeconds < 0 && trackingInfo.extendCount() >= MAX_EXTEND_COUNT) {
            throw new CustomException(ErrorCode.MAX_VISIBILITY_EXTENSION_REACHED);
        }

        return remainingSeconds <= VISIBILITY_THRESHOLD;
    }

    private void extendVisibilityTimeout(String queueUrl, String messageId) {
        MessageTrackingInfo trackingInfo = messageTracker.get(messageId);
        log.info("Extending visibility timeout for messageId = {}", messageId);

        if (trackingInfo.extendCount() >= MAX_EXTEND_COUNT) {
            log.warn("Maximum visibility extension count reached for messageId = {}, sending to DLQ", messageId);
            return;
        }

        ChangeMessageVisibilityRequest request = ChangeMessageVisibilityRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(trackingInfo.receiptHandle())
                .visibilityTimeout(VISIBILITY_EXTENSION)
                .build();

        sqsClient.changeMessageVisibility(request);

        // Update tracking info
        trackingInfo = new MessageTrackingInfo(
                trackingInfo.messageId(),
                trackingInfo.receiptHandle(),
                trackingInfo.receivedAt(),
                trackingInfo.visibilityTimeout() + VISIBILITY_EXTENSION,
                trackingInfo.extendCount() + 1
        );

        messageTracker.put(messageId, trackingInfo);
    }
}
