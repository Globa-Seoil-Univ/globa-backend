package org.y2k2.globa.application.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.user.command.CreateUserCommand;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

@Mapper()
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.profilePath", target = "profile")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.code", target = "code")
    @Mapping(source = "folderId", target = "publicFolderId")
    ResponseUserDto toResponseUserDto(UserEntity user, Long folderId);

    @Mapping(source = "code", target = "code")
    @Mapping(source = "cmd.snsKind", target = "snsKind")
    @Mapping(source = "cmd.snsId", target = "snsId")
    @Mapping(source = "cmd.name", target = "name")
    @Mapping(source = "cmd.profile", target = "profilePath")
    @Mapping(source = "cmd.notification", target = "primaryNofi")
    @Mapping(source = "cmd.notification", target = "uploadNofi")
    @Mapping(source = "cmd.notification", target = "shareNofi")
    @Mapping(source = "cmd.eventNotification", target = "eventNofi")
    UserEntity toEntity(CreateUserCommand cmd, String code);

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
