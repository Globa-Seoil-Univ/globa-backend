package org.y2k2.globa.application.record.dto.response;

import java.util.List;

public record ResponseRecordsDto(
        List<ResponseRecordDto> records,
        Long total
) {
}
