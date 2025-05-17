package org.y2k2.globa.application.record.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.record.command.CreateRecordCommand;
import org.y2k2.globa.application.record.mapper.RecordMapper;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Component
@RequiredArgsConstructor
public class CreateRecordUseCase implements UseCase<CreateRecordCommand, Long> {
    private final RecordRepository recordRepository;

    private final FileStore fileStore;

    @Override
    public Long execute(CreateRecordCommand command) {
        FileDto file = fileStore.getFile(command.dto().path())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD_FIREBASE));

        RecordEntity record = RecordMapper.INSTANCE.toEntity(command.dto(), command.folder(), command.user(), file.size());
        return recordRepository.save(record).getRecordId();
    }
}
