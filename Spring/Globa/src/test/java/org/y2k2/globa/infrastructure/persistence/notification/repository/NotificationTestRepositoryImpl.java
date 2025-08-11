package org.y2k2.globa.infrastructure.persistence.notification.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;

import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class NotificationTestRepositoryImpl {
    private final NotificationJpaRepository notificationRepository;

    public NotificationEntity save(NotificationEntity entity) {
        return notificationRepository.save(entity);
    }

    public List<NotificationEntity> saveAll(List<NotificationEntity> entities) {
        return notificationRepository.saveAll(entities);
    }
}
