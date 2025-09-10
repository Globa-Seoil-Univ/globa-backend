package org.y2k2.globa.infrastructure.persistence.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationProjection;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationUnReadCountProjection;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {
    @Query("""
        SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END
        FROM NotificationEntity n
        LEFT JOIN NotificationReadEntity nr ON n.notificationId = nr.notification.notificationId AND nr.isDeleted = false
        LEFT JOIN FolderShareEntity fs ON n.folderShare.shareId = fs.shareId
        LEFT JOIN FolderShareEntity fs2 ON n.folder.folderId = fs2.folder.folderId
            AND (fs2.ownerUser.userId = :userId OR fs2.targetUser.userId = :userId)
        WHERE nr.notification.notificationId IS NULL
        AND (
            (n.type = org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.NOTICE)
            OR (n.type = org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_INVITE AND fs.invitationStatus = 'PENDING' AND fs.targetUser.userId = :userId)
            OR (n.type IN (
                org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_FILE,
                org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_USER,
                org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_COMMENT
            ) AND fs2.invitationStatus = 'ACCEPT' AND n.sender.userId != :userId)
            OR (n.type IN (
                org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.UPLOAD_SUCCESS,
                org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.UPLOAD_FAILED,
                org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.INQUIRY
            ) AND n.receiver.userId = :userId)
        )
    """)
    Boolean existsByReceiver(Long userId);

    @Query(
            value = "SELECT " +
                    "n.notification_id AS notificationId, n.type_id AS type" +
                    ", n.share_id AS shareId, n.folder_id AS folderId" +
                    ", n.record_id AS recordId, n.comment_id AS commentId" +
                    ", n.notice_id AS noticeId, n.created_time AS createdTime" +
                    ", n.inquiry_id AS inquiryId" +
                    ", CASE WHEN nr.notification_id IS NOT NULL THEN TRUE ELSE FALSE END AS isRead" +
                    ", no.thumbnail_path AS noticeThumbnail, no.title AS noticeTitle, no.content AS noticeContent" +
                    ", f.title AS folderTitle, r.title AS recordTitle" +
                    ", c.content AS commentContent, i.title AS inquiryTitle" +
                    ", CASE " +
                        "WHEN n.type_id IN ('1', '6', '7', '8') THEN NULL " +
                        "WHEN n.type_id IN ('2', '3', '4', '5') THEN u2.profile_path " +
                    "END AS userProfile" +
                    ", CASE " +
                        "WHEN n.type_id IN ('1', '6', '7', '8') THEN NULL " +
                        "WHEN n.type_id IN ('2', '3', '4', '5') THEN u2.name " +
                    "END AS userName " +
                    "FROM notification n " +
                    "LEFT JOIN notification_read nr ON n.notification_id = nr.notification_id " +
                    "LEFT JOIN notice no ON n.notice_id = no.notice_id " +
                    "LEFT JOIN app_user u ON n.receiver_id = u.user_id " +
                    "LEFT JOIN app_user u2 ON n.sender_id = u2.user_id " +
                    "LEFT JOIN folder f ON n.folder_id = f.folder_id " +
                    "LEFT JOIN record r ON n.record_id = r.record_id " +
                    "LEFT JOIN comment c ON n.comment_id = c.comment_id " +
                    "LEFT JOIN inquiry i ON n.inquiry_id = i.inquiry_id " +
                    "LEFT JOIN folder_share fs ON n.share_id = fs.share_id " +
                    "WHERE " +
                    "(" +
                        "(:includeNotice = TRUE AND n.type_id = '1') " +
                            "OR (:includeInvite = TRUE AND n.type_id = '2' AND fs.invitation_status = 'PENDING' AND fs.target_id = :userId) " +
                            "OR (:includeShare = TRUE AND n.type_id IN ('3', '4', '5') AND n.sender_id != :userId " +
                            "AND EXISTS (" +
                                "SELECT 1 FROM folder_share fs_exists " +
                                "WHERE fs_exists.folder_id = n.folder_id " +
                                    "AND fs_exists.invitation_status = 'ACCEPT' " +
                                    "AND (fs_exists.owner_id = :userId OR fs_exists.target_id = :userId)" +
                            ")" +
                        ") " +
                            "OR (:includeRecord = TRUE AND n.type_id IN ('6', '7') AND n.receiver_id = :userId) " +
                            "OR (:includeInquiry = TRUE AND n.type_id = '8' AND n.receiver_id = :userId)" +
                    ")" +
                    "AND (nr.is_deleted = FALSE OR nr.is_deleted IS NULL) " +
                    "ORDER BY n.notification_id DESC",
            nativeQuery = true
    )
    Page<NotificationProjection> findAllByReceiverOrTypeIdInOrderByNotificationIdDesc(
            Long userId,
            boolean includeNotice,
            boolean includeInvite,
            boolean includeShare,
            boolean includeRecord,
            boolean includeInquiry,
            Pageable pageable
    );

    Optional<NotificationEntity> findByFolderFolderIdAndFolderShareShareIdAndReceiver_UserId(Long folderId, Long folderShareId, Long receiverId);
    Optional<NotificationEntity> findByNotificationId(Long notificationId);

    @Query(
            value = "SELECT " +
                    "COUNT(CASE WHEN n.type_id = '1' AND nr.notification_id IS NULL THEN 1 END) AS noticeCount, " +
                    "COUNT(CASE WHEN n.type_id = '2' AND fs.invitation_status = 'PENDING' AND nr.notification_id IS NULL AND fs.target_id = :userId THEN 1 END) AS inviteCount, " +
                    "COUNT(CASE WHEN n.type_id IN ('3', '4', '5') AND n.sender_id != :userId AND nr.notification_id IS NULL AND " +
                    "EXISTS(SELECT 1 FROM folder_share fs_check WHERE fs_check.folder_id = n.folder_id AND fs_check.invitation_status = 'ACCEPT' AND (fs_check.owner_id = :userId OR fs_check.target_id = :userId)) THEN 1 END) AS shareCount, " +
                    "COUNT(CASE WHEN n.type_id IN ('6', '7') AND n.receiver_id = :userId AND nr.notification_id IS NULL THEN 1 END) AS recordCount, " +
                    "COUNT(CASE WHEN n.type_id = '8' AND n.receiver_id = :userId AND nr.notification_id IS NULL THEN 1 END) AS inquiryCount " +
                    "FROM notification n " +
                    "LEFT JOIN notification_read nr ON n.notification_id = nr.notification_id " +
                    "LEFT JOIN folder_share fs ON n.share_id = fs.share_id " +
                    "WHERE (nr.is_deleted = FALSE OR nr.is_deleted IS NULL)",
            nativeQuery = true)
    NotificationUnReadCountProjection countByReceiverUserId(Long userId);
}
