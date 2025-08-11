package org.y2k2.globa.application.notification.dto.common;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithTopicDto extends SendMessage {
    private NoticeEntity notice;
    private String topic;
}
