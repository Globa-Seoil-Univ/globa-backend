package org.y2k2.globa.application.foldershare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyInvitationCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyInvitationUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;

@Service
@RequiredArgsConstructor
public class RefuseInvitationService {
    private final VerifyInvitationUseCase verifyInvitationUseCase;

    private final FolderShareRepository folderShareRepository;

    public void refuse(Long folderId, Long shareId, Long userId) {
        FolderShareEntity folderShare = folderShareRepository.getShareInvitation(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));

        if (!folderShare.getInvitationStatus().equals(InvitationStatus.PENDING)) {
            throw new CustomException(ErrorCode.SHARE_INVITATION_NOT_PENDING);
        }

        verifyInvitationUseCase.execute(
                VerifyInvitationCommand.of(folderShare, folderId, shareId, userId)
        );

        folderShareRepository.delete(folderShare);
    }
}
