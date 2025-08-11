package org.y2k2.globa.application.kafka.dto.request;

public record RequestKafkaDto(
        Long recordId,
        String userId,
        String lang
) {}
