package org.y2k2.globa.Projection;

import java.time.LocalDateTime;

public interface NotificationProjection {
    Long getNotificationId();
    Character getType();
    Long getShareId();
    Long getFolderId();
    Long getRecordId();
    Long getCommentId();
    Long getNoticeId();
    Long getInquiryId();
    Integer getIsRead();
    LocalDateTime getCreatedTime();
    String getNoticeThumbnail();
    String getNoticeTitle();
    String getNoticeContent();
    String getUserProfile();
    String getUserName();
    String getFolderTitle();
    String getRecordTitle();
    String getCommentContent();
    String getInquiryTitle();
}
