package org.y2k2.globa.application.foldershare.command;

import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

public record CreateFolderSharesCommand(
        FolderEntity folder,
        FolderRoleEntity role,
        UserEntity ownerUser,
        List<UserEntity> targetUsers,
        InvitationStatus status
) {
    public static CreateFolderSharesCommand of(
            FolderEntity folder,
            FolderRoleEntity role,
            UserEntity ownerUser,
            List<UserEntity> targetUsers,
            InvitationStatus status
    ) {
        return new CreateFolderSharesCommand(folder, role, ownerUser, targetUsers, status);
    }
}
