package org.y2k2.globa.application.folder.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.folder.dto.response.ResponseDetailFolderDto;
import org.y2k2.globa.application.folder.dto.response.ResponseFolderDto;
import org.y2k2.globa.insfrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.application.comment.mapper.CustomTimestampMapper;
import org.y2k2.globa.mapper.CustomTimestampTranslator;
import org.y2k2.globa.mapper.MapCreatedTime;

@Mapper(uses = CustomTimestampMapper.class)
public interface FolderMapper {
    FolderMapper INSTANCE = Mappers.getMapper(FolderMapper.class);

    @Mapping(source = "folderId", target = "folderId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ResponseFolderDto.FolderDto toResponseInFolderDto(FolderEntity folder);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ResponseDetailFolderDto toResponseDetailFolderDto(FolderEntity folder);

    @Mapping(source = "user", target = "user")
    @Mapping(source = "title", target = "title")
    FolderEntity toEntity(UserEntity user, String title);
}

