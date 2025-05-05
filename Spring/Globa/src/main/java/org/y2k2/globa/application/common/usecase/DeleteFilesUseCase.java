package org.y2k2.globa.application.common.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeleteFilesUseCase implements VoidUseCase<FolderEntity> {
    private final FileStore fileStore;

    private final RecordRepository recordRepository;

    @Async
    @Override
    public void execute(FolderEntity folder) {
        List<String> paths = recordRepository.getAllPath(folder.getFolderId());
        fileStore.deleteFiles(paths);
    }
}
