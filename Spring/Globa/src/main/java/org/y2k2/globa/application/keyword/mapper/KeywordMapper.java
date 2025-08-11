package org.y2k2.globa.application.keyword.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;
import org.y2k2.globa.application.keyword.dto.response.ResponseKeywordDto;

@Mapper
public interface KeywordMapper {
    KeywordMapper INSTANCE = Mappers.getMapper(KeywordMapper.class);

    @Mapping(source = "word", target = "word")
    @Mapping(source = "importance", target = "importance")
    ResponseKeywordDto toResponseKeywordDto(KeywordProjection projection);
}
