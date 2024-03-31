package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.DummyImageResponseDto;
import org.y2k2.globa.entity.DummyImageEntity;

@Mapper
public interface DummyImageMapper {
    DummyImageMapper INSTANCE = Mappers.getMapper(DummyImageMapper.class);

    @Mapping(source = "imageId", target = "imageId")
    @Mapping(source = "imagePath", target = "path")
    DummyImageResponseDto toResponseDto(DummyImageEntity imageEntity);
}
