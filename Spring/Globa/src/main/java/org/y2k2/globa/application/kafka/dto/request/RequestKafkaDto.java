package org.y2k2.globa.application.kafka.dto.request;

public record RequestKafkaDto(
        long recordId,
        long userId,
        String lang
) {}
