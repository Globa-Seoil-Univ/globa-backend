package org.y2k2.globa.application.userrole.command;

import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

public record SaveUserRoleCommand(
        UserEntity user,
        String roleName
) {
}
