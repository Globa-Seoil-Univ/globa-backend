package org.y2k2.globa.application.summary.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.summary.dto.response.ResponseDetailSummaryDto;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

@Mapper
public interface SummaryMapper {
    SummaryMapper INSTANCE = Mappers.getMapper(SummaryMapper.class);

    @Mapping(source = "content", target = "content")
    ResponseDetailSummaryDto toResponseDetailSummaryDto(SummaryEntity summary);
}
