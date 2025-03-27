package org.y2k2.globa.application.hightlight.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.hightlight.dto.response.ResponseDetailHighlightDto;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;

@Mapper
public interface HighlightMapper {
    HighlightMapper INSTANCE = Mappers.getMapper(HighlightMapper.class);

    @Mapping(source = "highlightId", target = "highlightId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "startIndex", target = "startIndex")
    @Mapping(source = "endIndex", target = "endIndex")
    ResponseDetailHighlightDto toResponseDetailHighlightDto(HighlightEntity highlight);
}
