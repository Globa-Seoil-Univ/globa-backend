package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import org.y2k2.globa.dto.common.file.FileDto;
import org.y2k2.globa.dto.response.dummyimage.ResponseDummyImageDto;
import org.y2k2.globa.entity.DummyImageEntity;

@Mapper
public interface DummyImageMapper {
    DummyImageMapper INSTANCE = Mappers.getMapper(DummyImageMapper.class);

    @Mapping(source = "imageId", target = "imageId")
    @Mapping(source = "imagePath", target = "path")
    ResponseDummyImageDto toResponseDto(DummyImageEntity imageEntity);

    @Mapping(source = "dto.storePath", target = "imagePath")
    @Mapping(source = "dto.extension", target = "imageType")
    @Mapping(source = "dto.size", target = "imageSize")
    DummyImageEntity toEntity(FileDto dto);
}
