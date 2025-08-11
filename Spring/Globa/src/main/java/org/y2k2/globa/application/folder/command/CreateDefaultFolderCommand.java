package org.y2k2.globa.application.folder.command;

import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

public record CreateDefaultFolderCommand(
        FolderRoleEntity folderRole,
        UserEntity user
) {
    public static CreateDefaultFolderCommand of(
            FolderRoleEntity folderRole,
            UserEntity user
    ) {
        return new CreateDefaultFolderCommand(folderRole, user);
    }
}
