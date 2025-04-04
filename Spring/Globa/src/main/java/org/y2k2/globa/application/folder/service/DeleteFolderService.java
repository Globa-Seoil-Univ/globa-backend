package org.y2k2.globa.application.folder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.common.usecase.DeleteFilesUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class DeleteFolderService {
    private final FindUserUseCase findUserUseCase;
    private final DeleteFilesUseCase deleteFilesUseCase;

    private final FolderRepository folderRepository;

    public void delete(Long folderId, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);
        FolderEntity folder = folderRepository.getFolderWithoutDefaultFolder(folderId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        folderRepository.delete(folder);
        deleteFilesUseCase.execute(folder);
    }
}
