package org.y2k2.globa.infrastructure.persistence.notificationread;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.notificationread.entity.NotificationReadEntity;
import org.y2k2.globa.infrastructure.persistence.notificationread.repository.NotificationReadJpaRepository;

import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class NotificationReadTestRepositoryImpl {
    private final NotificationReadJpaRepository notificationReadJpaRepository;

    public NotificationReadEntity save(NotificationReadEntity entity) {
        return notificationReadJpaRepository.save(entity);
    }

    public void saveAll(List<NotificationReadEntity> entities) {
        notificationReadJpaRepository.saveAll(entities);
    }
}
