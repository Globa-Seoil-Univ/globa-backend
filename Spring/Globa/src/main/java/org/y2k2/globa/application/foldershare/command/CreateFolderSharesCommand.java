package org.y2k2.globa.application.foldershare.command;

import org.y2k2.globa.application.folder.dto.request.RequestFolderPostDto;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

public record CreateFolderSharesCommand(
        FolderEntity folder,
        UserEntity ownerUser,
        List<TargetFolderShareCommand> targets,
        InvitationStatus status
) {
    public static CreateFolderSharesCommand of(
            FolderEntity folder,
            UserEntity ownerUser,
            List<TargetFolderShareCommand> targets,
            InvitationStatus status
    ) {
        return new CreateFolderSharesCommand(folder, ownerUser, targets, status);
    }
}
