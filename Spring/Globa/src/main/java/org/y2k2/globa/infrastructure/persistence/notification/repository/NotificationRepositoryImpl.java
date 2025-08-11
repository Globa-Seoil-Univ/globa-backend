package org.y2k2.globa.infrastructure.persistence.notification.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.application.notification.dto.common.NotificationParameters;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationProjection;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationUnReadCountProjection;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class NotificationRepositoryImpl implements NotificationRepository {
    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public void save(NotificationEntity entity) {
        notificationJpaRepository.save(entity);
    }

    @Override
    public void saveAll(List<NotificationEntity> entities) {
        notificationJpaRepository.saveAll(entities);
    }

    @Override
    public void delete(NotificationEntity entity) {
        notificationJpaRepository.delete(entity);
    }

    @Override
    public Boolean hasUnReadNotification(Long userId) {
        return notificationJpaRepository.existsByReceiver(userId);
    }

    @Override
    public Page<NotificationProjection> getNotifications(Long userId, NotificationParameters parameters, Pageable pageable) {
        return notificationJpaRepository.findAllByReceiverOrTypeIdInOrderByNotificationIdDesc(
                userId,
                parameters.getNotice(),
                parameters.getInvite(),
                parameters.getShare(),
                parameters.getRecord(),
                parameters.getInquiry(),
                pageable
        );
    }

    @Override
    public Optional<NotificationEntity> getReceivedNotification(Long folderId, Long folderShareId, Long receiverId) {
        return notificationJpaRepository.findByFolderFolderIdAndFolderShareShareIdAndReceiver_UserId(folderId, folderShareId, receiverId);
    }

    @Override
    public Optional<NotificationEntity> getNotification(Long notificationId) {
        return notificationJpaRepository.findByNotificationId(notificationId);
    }

    @Override
    public NotificationUnReadCountProjection getUnReadCount(Long userId) {
        return notificationJpaRepository.countByReceiverUserId(userId);
    }
}
