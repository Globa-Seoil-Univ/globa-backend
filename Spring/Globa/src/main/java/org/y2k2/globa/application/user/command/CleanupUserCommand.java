package org.y2k2.globa.application.user.command;

import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

public record CleanupUserCommand(
        List<UserEntity> users
) {
    public static CleanupUserCommand of(List<UserEntity> users) {
        return new CleanupUserCommand(users);
    }
}
