package org.y2k2.globa.application.notification.dto.common;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithFolderShareDto extends SendMessage {
    private FolderEntity folder;
    private FolderShareEntity folderShare;
}
