package org.y2k2.globa.application.notification.dto.common;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithUploadSuccessDto extends SendMessage {
    private FolderEntity folder;
    private RecordEntity record;
}
