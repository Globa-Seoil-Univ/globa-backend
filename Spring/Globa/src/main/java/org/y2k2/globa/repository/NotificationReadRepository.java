package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.NotificationReadEntity;

public interface NotificationReadRepository extends JpaRepository<NotificationReadEntity, Long> {
    NotificationReadEntity findByNotificationNotificationId(long notification_id);

}
