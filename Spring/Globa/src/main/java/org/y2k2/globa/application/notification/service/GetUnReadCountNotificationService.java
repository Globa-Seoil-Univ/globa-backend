package org.y2k2.globa.application.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadCountDto;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationUnReadCountProjection;

@Service
@RequiredArgsConstructor
public class GetUnReadCountNotificationService {
    private final NotificationRepository notificationRepository;

    public ResponseUnReadCountDto get(Long userId) {
        NotificationUnReadCountProjection notificationUnReadCountProjection = notificationRepository.getUnReadCount(userId);
        Long total = notificationUnReadCountProjection.getNoticeCount() +
                notificationUnReadCountProjection.getInviteCount() +
                notificationUnReadCountProjection.getShareCount() +
                notificationUnReadCountProjection.getRecordCount() +
                notificationUnReadCountProjection.getInquiryCount();

        return new ResponseUnReadCountDto(
                total,
                notificationUnReadCountProjection.getNoticeCount(),
                notificationUnReadCountProjection.getInviteCount() + notificationUnReadCountProjection.getShareCount(),
                notificationUnReadCountProjection.getRecordCount(),
                notificationUnReadCountProjection.getInquiryCount()
        );
    }
}
