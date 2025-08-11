package org.y2k2.globa.application.kafka.dto.response;

public record ResponseKafkaDto(
        Long recordId,
        String userId,
        String message
) {
}
