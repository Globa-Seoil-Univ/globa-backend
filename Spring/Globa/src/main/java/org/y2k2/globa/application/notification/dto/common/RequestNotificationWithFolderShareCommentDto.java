package org.y2k2.globa.application.notification.dto.common;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithFolderShareCommentDto extends SendMessage {
    private FolderEntity folder;
    private FolderShareEntity folderShare;
    private RecordEntity record;
    private CommentEntity comment;
}
