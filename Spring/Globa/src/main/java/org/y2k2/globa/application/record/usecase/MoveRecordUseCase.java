package org.y2k2.globa.application.record.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.record.command.MoveRecordCommand;
import org.y2k2.globa.common.annotation.FileCleanup;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Component
@RequiredArgsConstructor
public class MoveRecordUseCase implements VoidUseCase<MoveRecordCommand> {
    private final RecordRepository recordRepository;

    private final FileStore fileStore;

    @Override
    @FileCleanup
    public void execute(MoveRecordCommand command) {
        RecordEntity record = command.record();
        FolderEntity target = command.folder();

        String oldPath = command.record().getPath();
        String newPath = "folders/" + target.getFolderId() + oldPath.substring(oldPath.lastIndexOf("/"));
        fileStore.moveFile(oldPath, newPath);

        try {
            record.setFolder(target);
            record.setPath(newPath);
            recordRepository.save(record);
        } catch (Exception e) {
            throw new FileUploadException(newPath);
        }

        fileStore.deleteFile(oldPath);
    }
}
