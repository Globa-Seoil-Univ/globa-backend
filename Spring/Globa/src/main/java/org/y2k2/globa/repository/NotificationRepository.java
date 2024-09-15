package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.Projection.NotificationProjection;
import org.y2k2.globa.Projection.NotificationUnReadCount;
import org.y2k2.globa.entity.NotificationEntity;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    NotificationEntity findByFolderFolderIdAndFolderShareShareIdAndToUserUserId(long folderId, long folderShareId, long userId);
    NotificationEntity findByNotificationId(long notificationId);

    @Query(
            value = "SELECT DISTINCT n.notification_id AS notificationId, n.type_id AS type, " +
                        "n.share_id AS shareId, n.folder_id AS folderId, " +
                        "n.record_id AS recordId, n.comment_id AS commentId, " +
                        "n.notice_id AS noticeId, n.created_time AS createdTime, " +
                        "n.inquiry_id AS inquiryId, " +
                        "IF(nr.notification_id IS NOT NULL, TRUE, FALSE) AS isRead, " +
                        "no.thumbnail_path AS noticeThumbnail, no.title AS noticeTitle, no.content AS noticeContent, " +
                        "f.title AS folderTitle, r.title AS recordTitle, " +
                        "c.content AS commentContent, i.title AS inquiryTitle, " +
                        "CASE " +
                            "WHEN n.type_id IN ('1', '6', '7', '8') THEN NULL " +
                            "WHEN n.type_id IN ('2', '3', '4', '5') THEN u2.profile_path " +
                        "END AS userProfile, " +
                        "CASE " +
                            "WHEN n.type_id IN ('1', '6', '7', '8') THEN NULL " +
                            "WHEN n.type_id IN ('2', '3', '4', '5') THEN u2.name " +
                        "END AS userName " +
                    "FROM notification n " +
                    "LEFT JOIN notification_read nr ON n.notification_id = nr.notification_id " +
                    "LEFT JOIN notice no ON n.notice_id = no.notice_id " +
                    "LEFT JOIN app_user u ON n.to_user_id = u.user_id " +
                    "LEFT JOIN app_user u2 ON n.from_user_id = u2.user_id " +
                    "LEFT JOIN folder f ON n.folder_id = f.folder_id " +
                    "LEFT JOIN record r ON n.record_id = r.record_id " +
                    "LEFT JOIN comment c ON n.comment_id = c.comment_id " +
                    "LEFT JOIN inquiry i ON n.inquiry_id = i.inquiry_id " +
                    "LEFT JOIN folder_share fs ON n.share_id = fs.share_id " +
                    "LEFT JOIN folder_share fs2 ON n.folder_id = fs2.folder_id AND (fs2.owner_id = :userId OR fs2.target_id = :userId) " +
                    "WHERE " +
                        "(" +
                            "(:includeNotice = TRUE AND n.type_id = '1') " +
                            "OR (:includeInvite = TRUE AND n.type_id = '2' AND fs.invitation_status = 'PENDING' AND fs.target_id = :userId) " +
                            "OR (:includeShare = TRUE AND n.type_id IN ('3', '4', '5') AND fs2.invitation_status = 'ACCEPT' AND fs2.target_id != :userId) " +
                            "OR (:includeRecord = TRUE AND n.type_id IN ('6', '7') AND n.to_user_id = :userId) " +
                            "OR (:includeInquiry = TRUE AND n.type_id = '8' AND n.to_user_id = :userId)" +
                        ") AND (nr.is_deleted = FALSE OR nr.is_deleted IS NULL) " +
                    "ORDER BY n.created_time DESC",
            nativeQuery = true
    )
    Page<NotificationProjection> findAllByToUserOrTypeIdInOrderByCreatedTimeDesc(
            Pageable pageable,
            Long userId,
            boolean includeNotice,
            boolean includeInvite,
            boolean includeShare,
            boolean includeRecord,
            boolean includeInquiry
    );

    @Query(
            value = "SELECT " +
                        "COUNT(n.notification_id) != ( " +
                            "COUNT(CASE WHEN n.type_id = '1' AND nr.notification_id IS NOT NULL THEN 1 END) + " +
                            "COUNT(CASE WHEN n.type_id = '2' AND fs.invitation_status = 'PENDING' AND nr.notification_id IS NOT NULL AND fs.target_id = :userId THEN 1 END) + " +
                            "COUNT(CASE WHEN n.type_id IN ('3', '4', '5') AND fs2.invitation_status = 'ACCEPT' AND fs2.target_id != :userId AND nr.notification_id IS NOT NULL THEN 1 END) + " +
                            "COUNT(CASE WHEN n.type_id IN ('6', '7') AND n.to_user_id = :userId AND nr.notification_id IS NOT NULL THEN 1 END) + " +
                            "COUNT(CASE WHEN n.type_id = '8' AND n.to_user_id = :userId AND nr.notification_id IS NOT NULL THEN 1 END) " +
                        ") " +
                    "FROM notification n " +
                    "LEFT JOIN notification_read nr ON n.notification_id = nr.notification_id " +
                    "LEFT JOIN notice no ON n.notice_id = no.notice_id " +
                    "LEFT JOIN app_user u ON n.to_user_id = u.user_id " +
                    "LEFT JOIN app_user u2 ON n.from_user_id = u2.user_id " +
                    "LEFT JOIN folder f ON n.folder_id = f.folder_id " +
                    "LEFT JOIN record r ON n.record_id = r.record_id " +
                    "LEFT JOIN comment c ON n.comment_id = c.comment_id " +
                    "LEFT JOIN inquiry i ON n.inquiry_id = i.inquiry_id " +
                    "LEFT JOIN folder_share fs ON n.share_id = fs.share_id " +
                    "LEFT JOIN folder_share fs2 ON n.folder_id = fs2.folder_id AND (fs2.owner_id = :userId OR fs2.target_id = :userId) " +
                    "WHERE " +
                        "(" +
                            "n.type_id = '1' " +
                            "OR (n.type_id = '2' AND fs.invitation_status = 'PENDING' AND fs.target_id = :userId) " +
                            "OR n.type_id IN ('3', '4', '5') AND fs2.invitation_status = 'ACCEPT' AND fs2.target_id != :userId " +
                            "OR (n.type_id IN ('6', '7') AND n.to_user_id = :userId) " +
                            "OR (n.type_id = '8' AND n.to_user_id = :userId)" +
                        ") AND (nr.is_deleted = FALSE OR nr.is_deleted IS NULL)",
            nativeQuery = true
    )
    Long existsByToUser(Long userId);

    @Query(
            value = "SELECT " +
                        "COUNT(CASE WHEN n.type_id = '1' AND nr.notification_id IS NULL THEN 1 END) AS noticeCount, " +
                        "COUNT(CASE WHEN n.type_id = '2' AND fs.invitation_status = 'PENDING' AND nr.notification_id IS NULL AND fs.target_id = :userId THEN 1 END) AS inviteCount, " +
                        "COUNT(CASE WHEN n.type_id IN ('3', '4', '5') AND fs2.invitation_status = 'ACCEPT' AND fs2.target_id != :userId AND nr.notification_id IS NULL THEN 1 END) AS shareCount, " +
                        "COUNT(CASE WHEN n.type_id IN ('6', '7') AND n.to_user_id = :userId AND nr.notification_id IS NULL THEN 1 END) AS recordCount, " +
                        "COUNT(CASE WHEN n.type_id = '8' AND n.to_user_id = :userId AND nr.notification_id IS NULL THEN 1 END) AS inquiryCount " +
                    "FROM notification n " +
                    "LEFT JOIN notification_read nr ON n.notification_id = nr.notification_id " +
                    "LEFT JOIN notice no ON n.notice_id = no.notice_id " +
                    "LEFT JOIN app_user u ON n.to_user_id = u.user_id " +
                    "LEFT JOIN app_user u2 ON n.from_user_id = u2.user_id " +
                    "LEFT JOIN folder f ON n.folder_id = f.folder_id " +
                    "LEFT JOIN record r ON n.record_id = r.record_id " +
                    "LEFT JOIN comment c ON n.comment_id = c.comment_id " +
                    "LEFT JOIN inquiry i ON n.inquiry_id = i.inquiry_id " +
                    "LEFT JOIN folder_share fs ON n.share_id = fs.share_id " +
                    "LEFT JOIN folder_share fs2 ON n.folder_id = fs2.folder_id AND (fs2.owner_id = :userId OR fs2.target_id = :userId) " +
                    "WHERE " +
                        "(" +
                            "n.type_id = '1' " +
                            "OR (n.type_id = '2' AND fs.invitation_status = 'PENDING' AND fs.target_id = :userId) " +
                            "OR (n.type_id IN ('3', '4', '5') AND fs2.invitation_status = 'ACCEPT' AND fs2.target_id != :userId) " +
                            "OR (n.type_id IN ('6', '7') AND n.to_user_id = :userId) " +
                            "OR (n.type_id = '8' AND n.to_user_id = :userId)" +
                        ") AND (nr.is_deleted = FALSE OR nr.is_deleted IS NULL)",
            nativeQuery = true )
    NotificationUnReadCount countByToUserUserId(long userId);
}
