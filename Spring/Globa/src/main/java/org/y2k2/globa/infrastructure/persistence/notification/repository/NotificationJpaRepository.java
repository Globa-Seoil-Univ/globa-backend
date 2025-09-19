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

    @Query("SELECT " +
            "n.notificationId AS notificationId, " +
            "n.type AS type, " +
            "n.folderShare.shareId AS shareId, " +
            "n.folder.folderId AS folderId, " +
            "n.record.recordId AS recordId, " +
            "n.comment.commentId AS commentId, " +
            "n.notice.noticeId AS noticeId, " +
            "n.createdTime AS createdTime, " +
            "n.inquiry.inquiryId AS inquiryId, " +
            "CASE WHEN nr.notification.notificationId IS NOT NULL THEN true ELSE false END AS isRead, " +
            "no.thumbnailPath AS noticeThumbnail, " +
            "no.title AS noticeTitle, " +
            "no.content AS noticeContent, " +
            "f.title AS folderTitle, " +
            "r.title AS recordTitle, " +
            "c.content AS commentContent, " +
            "i.title AS inquiryTitle, " +
            "CASE " +
                "WHEN n.type IN (" +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.INQUIRY, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.NOTICE" +
                ") THEN NULL " +
                "WHEN n.type IN (" +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_INVITE, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.UPLOAD_SUCCESS, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.UPLOAD_FAILED, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_FILE, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_USER, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_COMMENT" +
                ") THEN u2.profilePath " +
            "END AS userProfile, " +
            "CASE " +
                "WHEN n.type IN (" +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.NOTICE, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.INQUIRY" +
                ") THEN NULL " +
                "WHEN n.type IN (" +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_INVITE, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.UPLOAD_SUCCESS, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.UPLOAD_FAILED, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_FILE, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_USER, " +
                    "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_COMMENT" +
                ") THEN u2.name " +
            "END AS userName " +
            "FROM NotificationEntity n " +
            "LEFT JOIN NotificationReadEntity nr ON n.notificationId = nr.notification.notificationId " +
            "LEFT JOIN NoticeEntity no ON n.notice.noticeId = no.noticeId " +
            "LEFT JOIN UserEntity u ON n.receiver.userId = u.userId " +
            "LEFT JOIN UserEntity u2 ON n.sender.userId = u2.userId " +
            "LEFT JOIN FolderEntity f ON n.folder.folderId = f.folderId " +
            "LEFT JOIN RecordEntity r ON n.record.recordId = r.recordId " +
            "LEFT JOIN CommentEntity c ON n.comment.commentId = c.commentId " +
            "LEFT JOIN InquiryEntity i ON n.inquiry.inquiryId = i.inquiryId " +
            "LEFT JOIN FolderShareEntity fs ON n.folderShare.shareId = fs.shareId " +
            "WHERE " +
            "(" +
                    "(" +
                        ":includeNotice = true " +
                        "AND n.type = org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.NOTICE" +
                    ") " +
                "OR (" +
                    ":includeInvite = true " +
                    "AND n.type = org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_INVITE " +
                    "AND fs.invitationStatus = 'PENDING' " +
                    "AND fs.targetUser.userId = :userId" +
                ") " +
                "OR (" +
                    ":includeShare = true " +
                    "AND n.type IN (" +
                        "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_FILE, " +
                        "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_USER, " +
                        "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.SHARE_FOLDER_ADD_COMMENT" +
                    ") " +
                    "AND n.sender.userId != :userId " +
                    "AND EXISTS (" +
                    "SELECT 1 FROM FolderShareEntity fsExists " +
                    "WHERE fsExists.folder.folderId = n.folder.folderId " +
                        "AND fsExists.invitationStatus = 'ACCEPT' " +
                        "AND (fsExists.ownerUser.userId = :userId OR fsExists.targetUser.userId = :userId)" +
                    ")" +
                ") " +
                "OR (" +
                    ":includeRecord = true " +
                    "AND n.type IN (" +
                        "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.UPLOAD_SUCCESS, " +
                        "org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.UPLOAD_FAILED" +
                    ") " +
                    "AND n.receiver.userId = :userId" +
                ") " +
                "OR (" +
                    ":includeInquiry = true " +
                    "AND n.type = org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType.INQUIRY " +
                    "AND n.receiver.userId = :userId" +
                ")" +
            ") " +
            "AND (nr.isDeleted = false OR nr.isDeleted IS NULL) " +
            "ORDER BY n.notificationId DESC"
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
            nativeQuery = true
    )
    NotificationUnReadCountProjection countByReceiverUserId(Long userId);
}
