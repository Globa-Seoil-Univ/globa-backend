package org.y2k2.globa.application.section.dto.response;

import org.y2k2.globa.application.analysis.dto.response.ResponseRecordAnalysisDto;
import org.y2k2.globa.application.summary.dto.response.ResponseDetailSummaryDto;

import java.util.List;

public record ResponseSectionDto(
        Long sectionId,
        String title,
        Long startTime,
        Long endTime,
        ResponseRecordAnalysisDto analyses,
        List<ResponseDetailSummaryDto> summaries,
        String createdTime
) {}
