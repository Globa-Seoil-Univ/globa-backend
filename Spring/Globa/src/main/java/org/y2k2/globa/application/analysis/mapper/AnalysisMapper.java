package org.y2k2.globa.application.analysis.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.analysis.dto.response.ResponseRecordAnalysisDto;
import org.y2k2.globa.application.hightlight.dto.response.ResponseDetailHighlightDto;
import org.y2k2.globa.entity.AnalysisEntity;

import java.util.List;

@Mapper
public interface AnalysisMapper {
    AnalysisMapper INSTANCE = Mappers.getMapper(AnalysisMapper.class);

    @Mapping(source = "analysis.analysisId", target = "analysisId")
    @Mapping(source = "analysis.content", target = "content")
    @Mapping(source = "highlights", target = "highlights")
    ResponseRecordAnalysisDto toResponseRecordAnalysisDto(AnalysisEntity analysis, List<ResponseDetailHighlightDto> highlights);
}
