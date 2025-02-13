package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.response.foldershare.ResponseFolderShareUserDto;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderRoleEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.type.InvitationStatus;

@Mapper
public interface FolderShareMapper {
    FolderShareMapper INSTANCE = Mappers.getMapper(FolderShareMapper.class);

    @Mapping(source = "shareEntity.role", target = "roleId", qualifiedByName = "MapRoleId")
    @Mapping(source = "shareEntity.shareId", target = "shareId")
    @Mapping(source = "shareEntity.targetUser.userId", target = "user.userId")
    @Mapping(source = "shareEntity.targetUser.profilePath", target = "user.profile")
    @Mapping(source = "shareEntity.targetUser.name", target = "user.name")
    @Mapping(source = "shareEntity.invitationStatus", target = "invitationStatus")
    ResponseFolderShareUserDto.FolderShareUserDto toShareUserDto(FolderShareEntity shareEntity);

    @Named("MapRoleId")
    default String mapRoleId(FolderRoleEntity folderRole) {
        return folderRole != null ? folderRole.getRoleId() : null;
    }

    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "invitationStatus", target = "invitationStatus")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "ownerUser", target = "ownerUser")
    @Mapping(source = "targetUser", target = "targetUser")
    @Mapping(target = "createdTime", ignore = true)
    FolderShareEntity toEntity(
            FolderEntity folder,
            InvitationStatus invitationStatus,
            FolderRoleEntity role,
            UserEntity ownerUser,
            UserEntity targetUser
    );
}
