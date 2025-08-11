package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.study.command.UpsertStudyCommand;
import org.y2k2.globa.application.study.dto.request.RequestStudyDto;
import org.y2k2.globa.application.study.usecase.UpsertStudyUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class UpsertStudyService {
    private final FindUserUseCase findUserUseCase;
    private final UpsertStudyUseCase upsertStudyUseCase;
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;

    private final RecordRepository recordRepository;

    public void upsert(Long folderId, Long recordId, RequestStudyDto dto, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);
        RecordEntity record = recordRepository.getRecord(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (!record.getFolder().getFolderId().equals(folderId)) {
            throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);
        }

        verifyFolderAccessibleUseCase.execute(VerifyFolderCommand.of(userId, folderId));

        upsertStudyUseCase.execute(
                UpsertStudyCommand.of(
                        user,
                        record,
                        dto.studyTime()
                )
        );
    }
}
