package org.y2k2.globa.application.sqs.dto.common;

import java.time.LocalDateTime;

public record ErrorInfo(
        TaskStep step,
        TaskType type,
        String message,
        LocalDateTime timestamp
) {
}
