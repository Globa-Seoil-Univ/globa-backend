package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.FolderShareUserDto;
import org.y2k2.globa.entity.FolderRoleEntity;
import org.y2k2.globa.entity.FolderShareEntity;

@Mapper
public interface FolderShareMapper {
    FolderShareMapper INSTANCE = Mappers.getMapper(FolderShareMapper.class);

    @Mapping(source = "shareEntity.roleId", target = "roleId", qualifiedByName = "MapRoleId")
    @Mapping(source = "shareEntity.targetUser.userId", target = "user.userId")
    @Mapping(source = "shareEntity.targetUser.profilePath", target = "user.profile")
    @Mapping(source = "shareEntity.targetUser.name", target = "user.name")
    @Mapping(source = "shareEntity.invitationStatus", target = "invitationStatus")
    FolderShareUserDto toShareUserDto(FolderShareEntity shareEntity);

    @Named("MapRoleId")
    default String mapRoleId(FolderRoleEntity folderRole) {
        return folderRole != null ? folderRole.getRoleId() : null;
    }
}
