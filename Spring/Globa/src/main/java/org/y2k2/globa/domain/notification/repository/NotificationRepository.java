package org.y2k2.globa.domain.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationProjection;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationUnReadCountProjection;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    void save(NotificationEntity entity);
    void saveAll(List<NotificationEntity> entities);
    void delete(NotificationEntity entity);

    Boolean hasUnReadNotification(Long userId);

    Page<NotificationProjection> getNotifications(
            Long userId,
            boolean includeNotice,
            boolean includeInvite,
            boolean includeShare,
            boolean includeRecord,
            boolean includeInquiry,
            Pageable pageable
    );

    Optional<NotificationEntity> getReceivedNotification(Long folderId, Long folderShareId, UserEntity receiver);
    Optional<NotificationEntity> getNotification(Long notificationId);

    NotificationUnReadCountProjection getUnReadCount(Long userId);
}
