package org.y2k2.globa.application.dictionary.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.dictionary.dto.common.DictionaryDto;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;

@Mapper
public interface DictionaryMapper {
    DictionaryMapper INSTANCE = Mappers.getMapper(DictionaryMapper.class);

    @Mapping(source = "word", target = "word")
    @Mapping(source = "engWord", target = "engWord")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "pronunciation", target = "pronunciation")
    DictionaryDto toDictionaryDto(DictionaryEntity entity);
}
