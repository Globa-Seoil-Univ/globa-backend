package org.y2k2.globa.application.notification.dto.common;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.common.type.NotificationType;

@Getter
@SuperBuilder
@ToString
public class SendMessage {
    private final UserEntity sender;
    private final UserEntity receiver;
    private final NotificationType notificationType;
    private final String title;
    private final String body;
}
