package org.y2k2.globa.dto.response.record;

import java.util.List;

public record ResponseRecordsDto(
        List<ResponseRecordDto> records,
        Long total
) {
}
