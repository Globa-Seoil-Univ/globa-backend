package org.y2k2.globa.dto.common.notification;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithFolderShareAddUserDto extends SendMessage {
    private FolderEntity folder;
    private FolderShareEntity folderShare;
}
