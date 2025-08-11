package org.y2k2.globa.application.notification.dto.response;

import org.y2k2.globa.application.notification.dto.common.NotificationDto;

import java.util.List;

public record ResponseNotificationDto(
    List<NotificationDto> notifications,
    Long total
) {
}
