package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.DictionaryDto;
import org.y2k2.globa.entity.DictionaryEntity;

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
