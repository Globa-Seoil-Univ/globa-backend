package org.y2k2.globa.application.record.dto.response;

import org.y2k2.globa.application.record.dto.request.RequestRecordDto;

import java.util.List;

public record ResponseRecordsByFolderDto(
        List<RequestRecordDto> records,
        boolean isOwner,
        Long total
) {}
