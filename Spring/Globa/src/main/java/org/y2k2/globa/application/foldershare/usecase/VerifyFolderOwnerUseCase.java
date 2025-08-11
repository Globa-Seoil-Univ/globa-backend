package org.y2k2.globa.application.foldershare.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;

@Component
@RequiredArgsConstructor
public class VerifyFolderOwnerUseCase implements VoidUseCase<VerifyFolderCommand> {
    private final FolderShareRepository folderShareRepository;

    @Override
    public void execute(VerifyFolderCommand command) {
        Boolean isOwner = folderShareRepository.isOwner(
                command.userId(),
                command.folderId()
        );

        if (!isOwner) {
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }
    }
}
