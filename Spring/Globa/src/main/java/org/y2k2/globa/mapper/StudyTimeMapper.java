package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.projection.StudyTimeProjection;
import org.y2k2.globa.dto.response.study.ResponseStudyTimesDto;
import org.y2k2.globa.entity.StudyEntity;

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
