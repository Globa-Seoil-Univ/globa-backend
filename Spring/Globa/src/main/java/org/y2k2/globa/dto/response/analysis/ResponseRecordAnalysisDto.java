package org.y2k2.globa.dto.response.analysis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.y2k2.globa.dto.response.highlights.ResponseDetailHighlightDto;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ResponseRecordAnalysisDto {
    private Long analysisId;
    private String content;
    private List<ResponseDetailHighlightDto> highlights;
}
