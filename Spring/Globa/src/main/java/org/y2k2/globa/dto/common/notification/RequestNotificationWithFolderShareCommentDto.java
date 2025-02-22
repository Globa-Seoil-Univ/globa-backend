package org.y2k2.globa.dto.common.notification;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.entity.*;

@Getter
@SuperBuilder
public class RequestNotificationWithFolderShareCommentDto extends SendMessage {
    private FolderEntity folder;
    private FolderShareEntity folderShare;
    private RecordEntity record;
    private CommentEntity comment;
}
