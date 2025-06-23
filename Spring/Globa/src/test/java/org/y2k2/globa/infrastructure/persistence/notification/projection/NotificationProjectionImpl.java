package org.y2k2.globa.infrastructure.persistence.notification.projection;

import java.time.LocalDateTime;

public class NotificationProjectionImpl implements NotificationProjection {
    Long notificationId;
    Character type;
    Long shareId;
    Long folderId;
    Long recordId;
    Long commentId;
    Long noticeId;
    Long inquiryId;
    Integer isRead;
    LocalDateTime createdTime;
    String noticeThumbnail;
    String noticeTitle;
    String noticeContent;
    String userProfile;
    String userName;
    String folderTitle;
    String recordTitle;
    String commentContent;
    String inquiryTitle;

    public NotificationProjectionImpl(Long notificationId, Character type, Long shareId, Long folderId,
                                      Long recordId, Long commentId, Long noticeId, Long inquiryId,
                                      Integer isRead, LocalDateTime createdTime, String noticeThumbnail,
                                      String noticeTitle, String noticeContent, String userProfile,
                                      String userName, String folderTitle, String recordTitle,
                                      String commentContent, String inquiryTitle) {
        this.notificationId = notificationId;
        this.type = type;
        this.shareId = shareId;
        this.folderId = folderId;
        this.recordId = recordId;
        this.commentId = commentId;
        this.noticeId = noticeId;
        this.inquiryId = inquiryId;
        this.isRead = isRead;
        this.createdTime = createdTime;
        this.noticeThumbnail = noticeThumbnail;
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.userProfile = userProfile;
        this.userName = userName;
        this.folderTitle = folderTitle;
        this.recordTitle = recordTitle;
        this.commentContent = commentContent;
        this.inquiryTitle = inquiryTitle;
    }


    @Override
    public Long getNotificationId() {
        return notificationId;
    }

    @Override
    public Character getType() {
        return type;
    }

    @Override
    public Long getShareId() {
        return shareId;
    }

    @Override
    public Long getFolderId() {
        return folderId;
    }

    @Override
    public Long getRecordId() {
        return recordId;
    }

    @Override
    public Long getCommentId() {
        return commentId;
    }

    @Override
    public Long getNoticeId() {
        return noticeId;
    }

    @Override
    public Long getInquiryId() {
        return inquiryId;
    }

    @Override
    public Integer getIsRead() {
        return isRead;
    }

    @Override
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    @Override
    public String getNoticeThumbnail() {
        return noticeThumbnail;
    }

    @Override
    public String getNoticeTitle() {
        return noticeTitle;
    }

    @Override
    public String getNoticeContent() {
        return noticeContent;
    }

    @Override
    public String getUserProfile() {
        return userProfile;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getFolderTitle() {
        return folderTitle;
    }

    @Override
    public String getRecordTitle() {
        return recordTitle;
    }

    @Override
    public String getCommentContent() {
        return commentContent;
    }

    @Override
    public String getInquiryTitle() {
        return inquiryTitle;
    }
}
