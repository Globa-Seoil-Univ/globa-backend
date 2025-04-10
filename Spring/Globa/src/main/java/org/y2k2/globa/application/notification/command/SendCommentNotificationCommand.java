package org.y2k2.globa.application.notification.command;

import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

public record SendCommentNotificationCommand(
        UserEntity user,
        FolderEntity folder,
        RecordEntity record,
        FolderShareEntity folderShare
) {
    public static SendCommentNotificationCommand of(
            UserEntity user,
            FolderEntity folder,
            RecordEntity record,
            FolderShareEntity folderShare
    ) {
        return new SendCommentNotificationCommand(user, folder, record, folderShare);
    }
}
