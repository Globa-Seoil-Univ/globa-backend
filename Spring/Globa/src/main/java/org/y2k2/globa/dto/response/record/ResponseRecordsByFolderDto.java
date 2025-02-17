package org.y2k2.globa.dto.response.record;

import org.y2k2.globa.dto.request.record.RequestRecordDto;

import java.util.List;

public record ResponseRecordsByFolderDto(
        List<RequestRecordDto> records,
        boolean isOwner,
        Long total
) {}
