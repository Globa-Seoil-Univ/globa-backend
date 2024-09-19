package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.FolderDto;
import org.y2k2.globa.dto.ResponseFolderDto;
import org.y2k2.globa.entity.FolderEntity;

@Mapper(uses = CustomTimestampMapper.class)
public interface FolderMapper {
    FolderMapper INSTANCE = Mappers.getMapper(FolderMapper.class);

    @Mapping(source = "folderEntity.folderId", target = "folderId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    FolderDto toFolderDto(FolderEntity folderEntity);

    @Mapping(source = "folderId", target = "folderId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ResponseFolderDto.FolderDto toResponseInFolderDto(FolderEntity folderEntity);
}

