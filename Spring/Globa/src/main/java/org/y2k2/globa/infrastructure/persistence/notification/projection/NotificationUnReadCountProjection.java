package org.y2k2.globa.infrastructure.persistence.notification.projection;

public interface NotificationUnReadCountProjection {
    Long getNoticeCount();
    Long getInviteCount();
    Long getShareCount();
    Long getRecordCount();
    Long getInquiryCount();
}
