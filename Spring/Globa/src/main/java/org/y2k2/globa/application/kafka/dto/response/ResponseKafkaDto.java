package org.y2k2.globa.application.kafka.dto.response;

public record ResponseKafkaDto(
        Long recordId,
        Long userId,
        String message
) {

}
