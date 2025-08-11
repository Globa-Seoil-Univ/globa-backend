package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderWritableUseCase;
import org.y2k2.globa.application.record.command.FindOwnRecordCommand;
import org.y2k2.globa.application.record.usecase.FindOwnRecordUseCase;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Service
@RequiredArgsConstructor
public class DeleteRecordService {
    private final VerifyFolderWritableUseCase verifyFolderWritableUseCase;
    private final FindOwnRecordUseCase findOwnRecordUseCase;

    private final RecordRepository recordRepository;

    private final FileStore fileStore;

    public void delete(Long folderId, Long recordId, Long userId) {
        RecordEntity record = findOwnRecordUseCase.execute(
                FindOwnRecordCommand.of(userId, folderId, recordId)
        );

        verifyFolderWritableUseCase.execute(
                VerifyFolderCommand.of(userId, folderId)
        );

        recordRepository.delete(record);
        fileStore.deleteFile(record.getPath());
    }
}
