package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.NotificationEntity;
import org.y2k2.globa.entity.UserEntity;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    NotificationEntity findByFolderFolderIdAndFolderShareShareIdAndToUserUserId(long folderId, long folderShareId, long userId);
    NotificationEntity findByNotificationId(long notificationId);

    Page<NotificationEntity> findAllByToUserOrTypeIdInOrderByCreatedTimeDesc(Pageable pageable, UserEntity user, char[] typeIds);

    List<NotificationEntity> findAllByToUserUserId(long userId);
    List<NotificationEntity> findAllByToUserUserIdAndIsReadAndTypeIdIn(long userId, boolean isRead, List<String> types);
    List<NotificationEntity> findAllByNotificationIdInAndTypeIdIn(List<Long> notificationIds, List<String> types);
}
