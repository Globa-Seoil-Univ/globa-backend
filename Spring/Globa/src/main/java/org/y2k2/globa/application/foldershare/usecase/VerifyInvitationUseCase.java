package org.y2k2.globa.application.foldershare.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.foldershare.command.VerifyInvitationCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;

@Component
@RequiredArgsConstructor
public class VerifyInvitationUseCase implements VoidUseCase<VerifyInvitationCommand> {
    @Override
    public void execute(VerifyInvitationCommand command) {
        FolderShareEntity folderShare = command.folderShare();

        boolean isShareIdMismatch = !folderShare.getShareId().equals(command.shareId());
        boolean isNotMineInvitation = !folderShare.getTargetUser().getUserId().equals(command.targetId());
        boolean isFolderIdMismatch = !folderShare.getFolder().getFolderId().equals(command.folderId());
        boolean isAlreadyAccepted = folderShare.getInvitationStatus().equals(InvitationStatus.ACCEPT);

        if (isShareIdMismatch) {
            throw new CustomException(ErrorCode.MISMATCH_SHARE_ID);
        } else if (isNotMineInvitation) {
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCEPT_INVITATION);
        } else if (isFolderIdMismatch) {
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_ID);
        } else if (isAlreadyAccepted) {
            throw new CustomException(ErrorCode.INVITE_ACCEPT_BAD_REQUEST);
        }
    }
}
