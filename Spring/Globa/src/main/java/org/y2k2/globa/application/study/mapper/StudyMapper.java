package org.y2k2.globa.application.study.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.common.mapper.CustomTimestampMapper;
import org.y2k2.globa.infrastructure.persistence.study.projection.StudyTimeProjection;
import org.y2k2.globa.application.study.dto.response.ResponseStudyTimesDto;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;

@Mapper(uses = {CustomTimestampMapper.class})
public interface StudyMapper {
    StudyMapper INSTANCE = Mappers.getMapper(StudyMapper.class);

    @Mapping(source = "totalStudyTime", target = "studyTime")
    @Mapping(source = "createdTime", target = "createdTime")
    ResponseStudyTimesDto toResponseTotalStudyTimesDto(StudyTimeProjection projection);

    @Mapping(source = "studyTime", target = "studyTime")
    @Mapping(source = "createdTime", target = "createdTime")
    ResponseStudyTimesDto toResponseStudyTimesDto(StudyEntity projection);
}
