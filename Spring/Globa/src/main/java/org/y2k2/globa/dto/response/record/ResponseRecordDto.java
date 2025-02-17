package org.y2k2.globa.dto.response.record;

import org.y2k2.globa.dto.response.keyword.ResponseKeywordDto;

import java.util.List;

public record ResponseRecordDto(
        Long recordId,
        Long folderId,
        String title,
        String path,
        List<ResponseKeywordDto> keywords,
        String createdTime
) {}
