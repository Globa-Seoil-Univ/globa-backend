package org.y2k2.globa.application.survey.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.y2k2.globa.application.survey.dto.request.RequestSurveyDto;
import org.y2k2.globa.infrastructure.persistence.survey.entity.SurveyEntity;

@Mapper
public interface SurveyMapper {
    SurveyMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(SurveyMapper.class);

    @Mapping(target = "surveyType", source = "surveyType")
    @Mapping(target = "content", source = "content")
    SurveyEntity toEntity(RequestSurveyDto requestSurveyDto);
}
