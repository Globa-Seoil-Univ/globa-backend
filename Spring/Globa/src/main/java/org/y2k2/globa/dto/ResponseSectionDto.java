package org.y2k2.globa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ResponseSectionDto {
    private Long sectionId;
    private String title;
    private Long startTime;
    private Long endTime;
    private ResponseRecordAnalysisDto analysis;
    private List<ResponseDetailSummaryDto> summary;
    private LocalDateTime createdTime;
}
