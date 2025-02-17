package org.y2k2.globa.dto.response.record;

import org.y2k2.globa.dto.response.section.ResponseSectionDto;
import org.y2k2.globa.dto.response.folder.ResponseDetailFolderDto;

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
