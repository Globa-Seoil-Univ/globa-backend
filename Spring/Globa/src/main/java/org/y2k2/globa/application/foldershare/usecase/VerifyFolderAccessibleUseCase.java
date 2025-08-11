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
public class VerifyFolderAccessibleUseCase implements VoidUseCase<VerifyFolderCommand> {
    private final FolderShareRepository folderShareRepository;

    @Override
    public void execute(VerifyFolderCommand command) {
        Boolean hasAccess = folderShareRepository.isAccessible(
                command.userId(),
                command.folderId()
        );

        if (!hasAccess) {
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
        }
    }
}
