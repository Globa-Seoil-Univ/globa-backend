package org.y2k2.globa.application.analysis.dto.response;

import org.y2k2.globa.application.hightlight.dto.response.ResponseDetailHighlightDto;

import java.util.List;

public record ResponseRecordAnalysisDto(
        Long analysisId,
        String content,
        List<ResponseDetailHighlightDto> highlights
) {}
