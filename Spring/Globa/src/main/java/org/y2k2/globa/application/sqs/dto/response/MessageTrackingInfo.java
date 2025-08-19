package org.y2k2.globa.application.sqs.dto.response;

import java.time.Instant;

public record MessageTrackingInfo(
        String messageId,
        String receiptHandle,
        Instant receivedAt,
        int visibilityTimeout,
        int extendCount
) {
}
