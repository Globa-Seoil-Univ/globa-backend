package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.response.highlights.ResponseDetailHighlightDto;
import org.y2k2.globa.dto.response.summary.ResponseDetailSummaryDto;
import org.y2k2.globa.entity.HighlightEntity;
import org.y2k2.globa.entity.SummaryEntity;

@Mapper
public interface SummaryMapper {
    SummaryMapper INSTANCE = Mappers.getMapper(SummaryMapper.class);

    @Mapping(source = "content", target = "content")
    ResponseDetailSummaryDto toResponseDetailSummaryDto(SummaryEntity summary);
}
