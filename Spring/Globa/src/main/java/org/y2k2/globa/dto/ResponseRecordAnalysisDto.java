package org.y2k2.globa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ResponseRecordAnalysisDto {
    private Long analysisId;
    private String content;
    private List<ResponseDetailHighlightDto> highlights;
}
