package org.y2k2.globa.application.record.dto.request;

public record IntroRecordDto(
        Long recordId,
        String title,
        String path,
        String createdTime
) {
}
