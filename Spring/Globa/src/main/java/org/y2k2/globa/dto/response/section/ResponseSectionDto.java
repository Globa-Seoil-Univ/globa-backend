package org.y2k2.globa.dto.response.section;

import org.y2k2.globa.dto.response.analysis.ResponseRecordAnalysisDto;
import org.y2k2.globa.dto.response.summary.ResponseDetailSummaryDto;

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
