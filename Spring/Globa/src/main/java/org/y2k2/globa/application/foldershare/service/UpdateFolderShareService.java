package org.y2k2.globa.application.foldershare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.FindFolderRoleUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.dto.request.RequestInviteDto;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;

@Service
@RequiredArgsConstructor
public class UpdateFolderShareService {
    private final FindFolderRoleUseCase findFolderRoleUseCase;
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
        FolderRoleEntity folderRole = findFolderRoleUseCase.execute(FolderRoleCommand.of(FolderRole.from(dto.role())))
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER_ROLE));

        folderShare.setRole(folderRole);
        folderShareRepository.save(folderShare);
    }
}
