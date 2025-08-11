package org.y2k2.globa.application.notification.command;

import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;

public record VerifyModifyNotificationCommand(
        NotificationEntity notification,
        Long userId
) {
    public static VerifyModifyNotificationCommand of(
            NotificationEntity notification,
            Long userId
    ) {
        return new VerifyModifyNotificationCommand(notification, userId);
    }
}
