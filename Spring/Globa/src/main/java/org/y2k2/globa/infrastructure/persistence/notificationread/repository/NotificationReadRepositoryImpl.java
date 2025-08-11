package org.y2k2.globa.infrastructure.persistence.notificationread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.notificationread.repository.NotificationReadRepository;
import org.y2k2.globa.infrastructure.persistence.notificationread.entity.NotificationReadEntity;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class NotificationReadRepositoryImpl implements NotificationReadRepository {
    private final NotificationReadJpaRepository notificationReadJpaRepository;

    @Override
    public void save(NotificationReadEntity entity) {
        notificationReadJpaRepository.save(entity);
    }

    @Override
    public Optional<NotificationReadEntity> getNotificationRead(Long notificationId) {
        return notificationReadJpaRepository.findByNotificationNotificationId(notificationId);
    }
}
