package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.record.command.FindOwnRecordCommand;
import org.y2k2.globa.application.record.usecase.FindOwnRecordUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Service
@RequiredArgsConstructor
public class UpdateShareLinkStatusService {
    private final FindOwnRecordUseCase findOwnRecordUseCase;

    private final FolderRepository folderRepository;
    private final RecordRepository recordRepository;

    public void update(Long folderId, Long recordId, boolean isShared, Long userId) {
        FolderEntity folder = folderRepository.getFolder(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        RecordEntity record = findOwnRecordUseCase.execute(
                FindOwnRecordCommand.of(userId, folderId, recordId)
        );

        if (!folder.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }

        record.setIsShare(isShared);
        recordRepository.save(record);
    }
}
