package org.y2k2.globa.application.foldershare.command;

import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;

public record VerifyInvitationCommand(
        FolderShareEntity folderShare,
        Long shareId,
        Long folderId,
        Long targetId
) {
    public static VerifyInvitationCommand of(
            FolderShareEntity folderShare,
            Long folderId,
            Long shareId,
            Long targetId
    ) {
        return new VerifyInvitationCommand(folderShare, shareId, folderId, targetId);
    }
}
