package org.y2k2.globa.dto.response.analysis;

import org.y2k2.globa.dto.response.highlights.ResponseDetailHighlightDto;

import java.util.List;

public record ResponseRecordAnalysisDto(
        Long analysisId,
        String content,
        List<ResponseDetailHighlightDto> highlights
) {}
