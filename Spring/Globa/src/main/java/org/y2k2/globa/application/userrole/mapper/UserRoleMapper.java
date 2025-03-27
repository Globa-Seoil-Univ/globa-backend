package org.y2k2.globa.application.userrole.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.userrole.command.CreateUserRoleCommand;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

@Mapper
public interface UserRoleMapper {
    UserRoleMapper INSTANCE = Mappers.getMapper(UserRoleMapper.class);

    @Mapping(source = "user", target = "user")
    @Mapping(source = "role", target = "role")
    UserRoleEntity toEntity(UserEntity user, RoleEntity role);
}
