package org.y2k2.globa.application.userrole.command;

import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

public record CreateUserRoleCommand(
        UserEntity user,
        UserRole roleName
) {
    public static CreateUserRoleCommand of(UserEntity user, UserRole roleName) {
        return new CreateUserRoleCommand(user, roleName);
    }
}
