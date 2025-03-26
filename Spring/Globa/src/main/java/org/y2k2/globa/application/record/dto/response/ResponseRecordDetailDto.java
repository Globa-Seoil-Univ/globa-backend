package org.y2k2.globa.application.record.dto.response;

import org.y2k2.globa.application.section.dto.response.ResponseSectionDto;
import org.y2k2.globa.application.folder.dto.response.ResponseDetailFolderDto;

import java.util.List;

public record ResponseRecordDetailDto(
        Long recordId,
        String title,
        String path,
        String size,
        ResponseDetailFolderDto folder,
        List<ResponseSectionDto> sections,
        String createdTime
) {}
