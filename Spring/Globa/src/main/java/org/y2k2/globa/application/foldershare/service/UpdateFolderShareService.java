package org.y2k2.globa.application.foldershare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.folderrole.command.GetFolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.GetFolderRoleUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.dto.request.RequestInviteDto;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class UpdateFolderShareService {
    private final GetFolderRoleUseCase getFolderRoleUseCase;
    private final VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;

    private final FolderShareRepository folderShareRepository;

    public void update(Long folderId, Long targetId, RequestInviteDto dto, Long ownerId) {
        if (targetId.equals(ownerId)) {
            throw new CustomException(ErrorCode.INVITE_BAD_REQUEST);
        }

        verifyFolderOwnerUseCase.execute(
                VerifyFolderCommand.of(ownerId, folderId)
        );

        FolderShareEntity folderShare = folderShareRepository.getShareInvitation(folderId, targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));
        FolderRoleEntity folderRole = getFolderRoleUseCase.execute(
                GetFolderRoleCommand.of(FolderRole.from(dto.role()))
        );

        folderShare.setRole(folderRole);
        folderShareRepository.save(folderShare);
    }
}
