package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.NotificationReadEntity;

import java.util.Optional;

public interface NotificationReadRepository extends JpaRepository<NotificationReadEntity, Long> {
    Optional<NotificationReadEntity> findByNotificationNotificationId(long notification_id);
}
