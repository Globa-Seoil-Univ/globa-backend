package org.y2k2.globa.application.folder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.folder.command.UpdateFolderNameCommand;
import org.y2k2.globa.application.folder.usecase.UpdateFolderNameUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;

@Service
@RequiredArgsConstructor
public class UpdateFolderNameService {
    private final UpdateFolderNameUseCase updateFolderNameUseCase;

    private final FolderRepository folderRepository;
    private final FolderShareRepository folderShareRepository;

    public void update(Long folderId, String title, Long userId) {
        if (folderShareRepository.isOwner(userId, folderId)) {
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }

        FolderEntity folder = folderRepository.getFolder(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        updateFolderNameUseCase.execute(
                UpdateFolderNameCommand.of(folder, title)
        );
    }
}
