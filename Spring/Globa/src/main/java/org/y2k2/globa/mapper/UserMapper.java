package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.request.user.RequestUserPostDTO;
import org.y2k2.globa.dto.response.user.ResponseNotificationSettingDto;
import org.y2k2.globa.dto.response.user.ResponseUserDto;
import org.y2k2.globa.dto.response.user.ResponseUserSearchDto;
import org.y2k2.globa.entity.UserEntity;

@Mapper()
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.profilePath", target = "profile")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.code", target = "code")
    @Mapping(source = "folderId", target = "publicFolderId")
    ResponseUserDto toResponseUserDto(UserEntity user, Long folderId);

    @Mapping(source = "snsKind", target = "snsKind")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "dto.snsId", target = "snsId")
    @Mapping(source = "dto.name", target = "name")
    @Mapping(source = "dto.profile", target = "profilePath")
    @Mapping(source = "dto.notification", target = "primaryNofi")
    @Mapping(source = "dto.notification", target = "uploadNofi")
    @Mapping(source = "dto.notification", target = "shareNofi")
    @Mapping(source = "dto.eventNotification", target = "eventNofi")
    UserEntity toEntity(String snsKind, String code, RequestUserPostDTO dto);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.profilePath", target = "profile")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.code", target = "code")
    ResponseUserSearchDto toResponseUserSearchDto(UserEntity user);

    @Mapping(source = "user.uploadNofi", target = "uploadNofi")
    @Mapping(source = "user.shareNofi", target = "shareNofi")
    @Mapping(source = "user.eventNofi", target = "eventNofi")
    ResponseNotificationSettingDto toResponseNotificationSettingDto(UserEntity user);
}
