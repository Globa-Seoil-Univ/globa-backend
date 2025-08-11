package org.y2k2.globa.domain.notificationread.repository;

import org.y2k2.globa.infrastructure.persistence.notificationread.entity.NotificationReadEntity;

import java.util.Optional;

public interface NotificationReadRepository {
    void save(NotificationReadEntity entity);

    Optional<NotificationReadEntity> getNotificationRead(Long notificationId);
}
