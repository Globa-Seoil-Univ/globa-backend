package org.y2k2.globa.application.sqs.dto.request;

public record RequestSQSDto(
        Long recordId,
        String userId,
        String lang
) {}
