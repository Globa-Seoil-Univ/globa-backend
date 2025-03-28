package org.y2k2.globa.application.studytime.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.common.mapper.CustomTimestampMapper;
import org.y2k2.globa.application.common.mapper.CustomTimestampTranslator;
import org.y2k2.globa.application.common.mapper.MapCreatedTime;
import org.y2k2.globa.infrastructure.persistence.study.projection.StudyTimeProjection;
import org.y2k2.globa.application.studytime.dto.response.ResponseStudyTimesDto;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;

@Mapper(uses = {CustomTimestampMapper.class})
public interface StudyTimeMapper {
    StudyTimeMapper INSTANCE = Mappers.getMapper(StudyTimeMapper.class);

    @Mapping(source = "totalStudyTime", target = "studyTime")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = {CustomTimestampTranslator.class, MapCreatedTime.class})
    ResponseStudyTimesDto toResponseTotalStudyTimesDto(StudyTimeProjection projection);

    @Mapping(source = "studyTime", target = "studyTime")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = {CustomTimestampTranslator.class, MapCreatedTime.class})
    ResponseStudyTimesDto toResponseStudyTimesDto(StudyEntity projection);
}
