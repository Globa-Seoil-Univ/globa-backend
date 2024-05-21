package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.NotificationEntity;
import org.y2k2.globa.entity.UserEntity;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    NotificationEntity findByFolderFolderIdAndFolderShareShareIdAndFromUserUserId(long folderId, long folderShareId, long userId);

    Page<NotificationEntity> findAllByFromUserOrTypeIdIn(Pageable pageable, UserEntity user, char[] typeIds);
}
