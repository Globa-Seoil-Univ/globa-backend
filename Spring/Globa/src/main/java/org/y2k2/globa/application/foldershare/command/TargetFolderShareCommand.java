package org.y2k2.globa.application.foldershare.command;

import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

public record TargetFolderShareCommand(
        FolderRoleEntity role,
        UserEntity user
) {
}
