package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.record.command.CombineRecordsAndKeywordsCommand;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
import org.y2k2.globa.application.record.usecase.CombineRecordsAndKeywordsUseCase;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Service
@RequiredArgsConstructor
public class GetSharingRecordService {
    private final CombineRecordsAndKeywordsUseCase combineRecordsAndKeywordsUseCase;

    private final RecordRepository recordRepository;

    public ResponseRecordsDto get(int page, int count, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, count);
        Page<RecordEntity> records = recordRepository.getOwnedRecord(userId, pageable);

        return combineRecordsAndKeywordsUseCase.execute(
                CombineRecordsAndKeywordsCommand.of(
                        records.getContent(),
                        records.getTotalElements()
                )
        );
    }
}
