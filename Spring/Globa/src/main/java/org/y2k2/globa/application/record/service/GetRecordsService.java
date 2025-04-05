package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsByFolderDto;
import org.y2k2.globa.application.record.mapper.RecordMapper;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Service
@RequiredArgsConstructor
public class GetRecordsService {
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;

    private final FolderRepository folderRepository;
    private final RecordRepository recordRepository;

    public ResponseRecordsByFolderDto get(Long folderId, int page, int count, Long userId) {
        FolderEntity folder = folderRepository.getFolder(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        verifyFolderAccessibleUseCase.execute(
                VerifyFolderCommand.of(
                        userId,
                        folderId
                )
        );

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<RecordEntity> records = recordRepository.getRecordsByFolderId(folder.getFolderId(), pageable);

        boolean isOwner = folder.getUser().getUserId().equals(userId);

        return new ResponseRecordsByFolderDto(
                records.stream().map(RecordMapper.INSTANCE::toRequestRecordDto).toList(),
                isOwner,
                records.getTotalElements()
        );
    }
}
