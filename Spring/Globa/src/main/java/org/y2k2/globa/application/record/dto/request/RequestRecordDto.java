package org.y2k2.globa.application.record.dto.request;

import java.time.LocalDateTime;

public record RequestRecordDto(
        Long recordId,
        String title,
        String path,
        LocalDateTime createdTime
) {
}
