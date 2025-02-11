package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.dto.response.keyword.ResponseKeywordDto;

@Mapper
public interface KeywordMapper {
    KeywordMapper INSTANCE = Mappers.getMapper(KeywordMapper.class);

    @Mapping(source = "word", target = "word")
    @Mapping(source = "importance", target = "importance")
    ResponseKeywordDto toResponseKeywordDto(KeywordProjection projection);
}
