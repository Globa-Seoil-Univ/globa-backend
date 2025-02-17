package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.response.highlights.ResponseDetailHighlightDto;
import org.y2k2.globa.entity.HighlightEntity;

@Mapper
public interface HighlightMapper {
    HighlightMapper INSTANCE = Mappers.getMapper(HighlightMapper.class);

    @Mapping(source = "highlightId", target = "highlightId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "startIndex", target = "startIndex")
    @Mapping(source = "endIndex", target = "endIndex")
    ResponseDetailHighlightDto toResponseDetailHighlightDto(HighlightEntity highlight);
}
