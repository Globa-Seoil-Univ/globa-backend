package org.y2k2.globa.application.record.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.record.command.CleanupRecordCommand;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.record.repository.RecordRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CleanupRecordUseCase implements VoidUseCase<CleanupRecordCommand> {
    private final RecordRepository recordRepository;
    private final FileStore fileStore;

    @Override
    public void execute(CleanupRecordCommand command) {
        List<String> storedPaths = recordRepository.getAllPathWIthUserIds(command.userIds());

        if (storedPaths.isEmpty()) {
            return;
        }

        fileStore.deleteFiles(storedPaths);
    }
}
