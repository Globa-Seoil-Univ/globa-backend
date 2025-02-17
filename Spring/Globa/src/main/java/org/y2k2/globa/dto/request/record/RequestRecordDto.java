package org.y2k2.globa.dto.request.record;

import java.time.LocalDateTime;

public record RequestRecordDto(
        Long recordId,
        String title,
        String path,
        LocalDateTime createdTime
) {
}
