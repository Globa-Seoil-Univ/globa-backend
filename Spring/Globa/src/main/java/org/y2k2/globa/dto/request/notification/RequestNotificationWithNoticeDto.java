package org.y2k2.globa.dto.request.notification;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.dto.common.fcm.SendMessage;
import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.entity.UserEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithNoticeDto extends SendMessage {
    private UserEntity fromUser;
    private NoticeEntity notice;
}
