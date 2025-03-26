package org.y2k2.globa.application.user.command;

import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

public record SaveUserCommand(
        UserEntity user
) {
}
