package org.y2k2.globa.infrastructure.persistence.notificationread.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.notificationread.entity.NotificationReadEntity;

import java.util.Optional;

public interface NotificationReadJpaRepository extends JpaRepository<NotificationReadEntity, Long> {
    Optional<NotificationReadEntity> findByNotificationNotificationId(Long notificationId);
}
