package org.y2k2.globa.application.folder.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.folder.command.FindFolderCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;

@Component
@RequiredArgsConstructor
public class FindFolderAndThrowUseCase implements UseCase<FindFolderCommand, FolderEntity> {
    private final FolderRepository folderRepository;

    @Override
    public FolderEntity execute(FindFolderCommand command) {
        FolderEntity folder = folderRepository.getFolder(command.folderId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        if (!folder.getUser().getUserId().equals(command.ownerId())) {
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }

        return folder;
    }
}
