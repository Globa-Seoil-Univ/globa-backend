package org.y2k2.globa.application.record.command;

import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

public record MoveRecordCommand(
        RecordEntity record,
        FolderEntity folder
) {
    public static MoveRecordCommand of(RecordEntity record, FolderEntity folder) {
        return new MoveRecordCommand(record, folder);
    }
}
