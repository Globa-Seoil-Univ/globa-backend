package org.y2k2.globa.dto.response.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.dto.common.notification.NotificationDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseNotificationDto {
    private List<NotificationDto> notifications;
    private long total;
}
