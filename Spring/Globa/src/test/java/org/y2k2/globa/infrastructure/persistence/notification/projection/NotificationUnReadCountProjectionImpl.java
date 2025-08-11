package org.y2k2.globa.infrastructure.persistence.notification.projection;

public class NotificationUnReadCountProjectionImpl implements NotificationUnReadCountProjection {
    Long noticeCount;
    Long inviteCount;
    Long shareCount;
    Long recordCount;
    Long inquiryCount;

    public NotificationUnReadCountProjectionImpl(Long noticeCount, Long inviteCount, Long shareCount, Long recordCount, Long inquiryCount) {
        this.noticeCount = noticeCount;
        this.inviteCount = inviteCount;
        this.shareCount = shareCount;
        this.recordCount = recordCount;
        this.inquiryCount = inquiryCount;
    }

    @Override
    public Long getNoticeCount() {
        return noticeCount;
    }

    @Override
    public Long getInviteCount() {
        return inviteCount;
    }

    @Override
    public Long getShareCount() {
        return shareCount;
    }

    @Override
    public Long getRecordCount() {
        return recordCount;
    }

    @Override
    public Long getInquiryCount() {
        return inquiryCount;
    }
}
