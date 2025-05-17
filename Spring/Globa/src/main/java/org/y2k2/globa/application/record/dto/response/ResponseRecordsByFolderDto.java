package org.y2k2.globa.application.record.dto.response;

import org.y2k2.globa.application.record.dto.request.IntroRecordDto;

import java.util.List;

public record ResponseRecordsByFolderDto(
        List<IntroRecordDto> records,
        boolean isOwner,
        Long total
) {}
