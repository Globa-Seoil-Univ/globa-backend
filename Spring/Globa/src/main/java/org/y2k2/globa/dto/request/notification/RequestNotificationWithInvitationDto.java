package org.y2k2.globa.dto.request.notification;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.dto.common.fcm.SendMessage;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithInvitationDto extends SendMessage {
    private FolderEntity folder;
    private FolderShareEntity folderShare;
}
