package org.y2k2.globa.application.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadNotificationDto;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class HasUnReadNotificationService {
    private final NotificationRepository notificationRepository;

    public ResponseUnReadNotificationDto get(Long userId) {
        return new ResponseUnReadNotificationDto(
                notificationRepository.hasUnReadNotification(userId)
        );
    }
}
