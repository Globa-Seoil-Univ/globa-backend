package org.y2k2.globa.application.record.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.record.command.FindOwnRecordCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Component
@RequiredArgsConstructor
public class FindOwnRecordUseCase implements UseCase<FindOwnRecordCommand, RecordEntity> {
    private final RecordRepository recordRepository;
    private final FolderShareRepository folderShareRepository;

    @Override
    public RecordEntity execute(FindOwnRecordCommand command) {
        RecordEntity record = recordRepository.getRecord(command.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        Boolean isOwner = folderShareRepository.isOwner(command.userId(), command.folderId());

        if (!record.getUser().getUserId().equals(command.userId()) && !isOwner) {
            throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
        } else if (!record.getFolder().getFolderId().equals(command.folderId())) {
            throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);
        }

        return record;
    }
}
