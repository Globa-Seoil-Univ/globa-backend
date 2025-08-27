package org.y2k2.globa.application.sqs.dto.response;

public record ResponseSQSDto(
        Long recordId,
        String userId,
        String status,
        String message
) {
}
