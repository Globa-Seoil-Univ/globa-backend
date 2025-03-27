package org.y2k2.globa.application.userrole.command;

import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

public record CreateUserRoleCommand(
        UserEntity user,
        String roleName
) {
    public static CreateUserRoleCommand of(UserEntity user, String roleName) {
        return new CreateUserRoleCommand(user, roleName);
    }
}
