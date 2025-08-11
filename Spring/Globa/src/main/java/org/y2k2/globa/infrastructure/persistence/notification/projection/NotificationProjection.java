package org.y2k2.globa.infrastructure.persistence.notification.projection;

import java.time.LocalDateTime;

public interface NotificationProjection {
    Long getNotificationId();
    String getType();
    Boolean getIsRead();
    Long getFolderId();
    String getFolderTitle();
    Long getRecordId();
    String getRecordTitle();
    Long getShareId();
    Long getCommentId();
    String getCommentContent();
    Long getNoticeId();
    String getNoticeThumbnail();
    String getNoticeTitle();
    String getNoticeContent();
    LocalDateTime getCreatedTime();
    String getUserProfile();
    String getUserName();
    Long getInquiryId();
    String getInquiryTitle();
}
