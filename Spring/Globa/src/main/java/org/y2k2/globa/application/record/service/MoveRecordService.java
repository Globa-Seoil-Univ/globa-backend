package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.application.record.command.FindOwnRecordCommand;
import org.y2k2.globa.application.record.command.MoveRecordCommand;
import org.y2k2.globa.application.record.dto.request.RequestRecordMoveDto;
import org.y2k2.globa.application.record.usecase.FindOwnRecordUseCase;
import org.y2k2.globa.application.record.usecase.MoveRecordUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Service
@RequiredArgsConstructor
public class MoveRecordService {
    private final FindOwnRecordUseCase findOwnRecordUseCase;
    private final VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    private final MoveRecordUseCase moveRecordUseCase;

    private final FolderRepository folderRepository;
    private final RecordRepository recordRepository;

    public void move(Long folderId, Long recordId, RequestRecordMoveDto dto, Long userId) {
        RecordEntity record = findOwnRecordUseCase.execute(
                FindOwnRecordCommand.of(userId, folderId, recordId)
        );
        FolderEntity target = folderRepository.getFolder(dto.targetId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_TARGET_FOLDER));

        verifyFolderOwnerUseCase.execute(VerifyFolderCommand.of(userId, folderId));
        verifyFolderAccessibleUseCase.execute(VerifyFolderCommand.of(userId, dto.targetId()));

        moveRecordUseCase.execute(
                MoveRecordCommand.of(
                        record,
                        target
                )
        );
    }
}
