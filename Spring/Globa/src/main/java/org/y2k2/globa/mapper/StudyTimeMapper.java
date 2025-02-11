package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.Projection.StudyTimeProjection;
import org.y2k2.globa.dto.response.study.ResponseStudyTimesDto;

@Mapper
public interface StudyTimeMapper {
    StudyTimeMapper INSTANCE = Mappers.getMapper(StudyTimeMapper.class);

    @Mapping(source = "totalStudyTime", target = "studyTime")
    @Mapping(source = "createdTime", target = "createdTime")
    ResponseStudyTimesDto toResponseStudyTimesDto(StudyTimeProjection projection);
}
