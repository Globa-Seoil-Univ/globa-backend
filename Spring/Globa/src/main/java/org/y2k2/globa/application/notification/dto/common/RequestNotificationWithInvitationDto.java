package org.y2k2.globa.application.notification.dto.common;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.insfrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.insfrastructure.persistence.foldershare.entity.FolderShareEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithInvitationDto extends SendMessage {
    private FolderEntity folder;
    private FolderShareEntity folderShare;
}
