package org.y2k2.globa.dto.common.notification;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.entity.NoticeEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithTopicDto extends SendMessage {
    private NoticeEntity notice;
    private String topic;
}
