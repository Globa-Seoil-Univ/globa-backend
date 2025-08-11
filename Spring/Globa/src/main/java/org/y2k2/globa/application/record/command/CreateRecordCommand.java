package org.y2k2.globa.application.record.command;

import org.y2k2.globa.application.record.dto.request.RequestPostRecordDto;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

public record CreateRecordCommand(
        FolderEntity folder,
        UserEntity user,
        RequestPostRecordDto dto
) {
    public static CreateRecordCommand of(FolderEntity folder, UserEntity user, RequestPostRecordDto dto) {
        return new CreateRecordCommand(folder, user, dto);
    }
}
