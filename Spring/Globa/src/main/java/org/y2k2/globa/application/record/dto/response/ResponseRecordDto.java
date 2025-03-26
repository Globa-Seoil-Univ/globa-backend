package org.y2k2.globa.application.record.dto.response;

import org.y2k2.globa.application.keyword.dto.response.ResponseKeywordDto;

import java.util.List;

public record ResponseRecordDto(
        Long recordId,
        Long folderId,
        String title,
        String path,
        List<ResponseKeywordDto> keywords,
        String createdTime
) {}
