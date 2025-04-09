package org.y2k2.globa.application.foldershare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.folder.command.FindFolderCommand;
import org.y2k2.globa.application.folder.usecase.FindFolderAndThrowUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class DeleteFolderShareService {
    private final FindUserUseCase findUserUseCase;
    private final FindFolderAndThrowUseCase findFolderAndThrowUseCase;

    private final FolderShareRepository folderShareRepository;

    public void delete(Long folderId, Long targetId, Long ownerId) {
        if (targetId.equals(ownerId)) {
            throw new CustomException(ErrorCode.INVITE_BAD_REQUEST);
        }

        UserEntity target = findUserUseCase.execute(targetId);
        FolderEntity folder = findFolderAndThrowUseCase.execute(FindFolderCommand.of(folderId, ownerId));
        FolderShareEntity folderShare = folderShareRepository.getShareInvitation(folder, target)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));

        folderShareRepository.delete(folderShare);
    }
}
