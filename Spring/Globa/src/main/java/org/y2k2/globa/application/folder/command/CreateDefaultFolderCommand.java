package org.y2k2.globa.application.folder.command;

import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

public record CreateDefaultFolderCommand(
        UserEntity user
) {
    public static CreateDefaultFolderCommand of(UserEntity user) {
        return new CreateDefaultFolderCommand(user);
    }
}
