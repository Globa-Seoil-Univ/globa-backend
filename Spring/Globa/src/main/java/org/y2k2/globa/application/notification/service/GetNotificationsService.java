package org.y2k2.globa.application.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.notification.dto.common.NotificationDto;
import org.y2k2.globa.application.notification.dto.common.NotificationParameters;
import org.y2k2.globa.application.notification.dto.response.ResponseNotificationDto;
import org.y2k2.globa.application.notification.mapper.NotificationMapper;
import org.y2k2.globa.common.type.NotificationSort;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationProjection;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetNotificationsService {
    private final NotificationRepository notificationRepository;

    public ResponseNotificationDto get(int count, int page, NotificationSort sort, Long userId) {
        NotificationParameters parameters = getNotificationParameters(sort);
        Pageable pageable = PageRequest.of(page - 1, count);
        Page<NotificationProjection> notifications = notificationRepository.getNotifications(
                userId,
                parameters,
                pageable
        );

        List<NotificationDto> response = notifications.getContent().stream()
                .map(NotificationMapper.INSTANCE::toResponseNotificationDto)
                .toList();

        return new ResponseNotificationDto(response, notifications.getTotalElements());
    }

    private NotificationParameters getNotificationParameters(NotificationSort sort) {
        NotificationParameters parameters = new NotificationParameters();

        switch (sort) {
            case NOTICE:
                parameters.setNotice(true);
                break;
            case SHARE:
                parameters.setShare(true);
                parameters.setInvite(true);
                break;
            case INQUIRY:
                parameters.setInquiry(true);
                break;
            case RECORD:
                parameters.setRecord(true);
                break;
            default:
                parameters.setAllTrue();
                break;
        }

        return parameters;
    }
}
