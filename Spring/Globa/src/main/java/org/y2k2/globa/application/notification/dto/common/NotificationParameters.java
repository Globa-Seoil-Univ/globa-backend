package org.y2k2.globa.application.notification.dto.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NotificationParameters {
    private Boolean notice;
    private Boolean invite;
    private Boolean share;
    private Boolean record;
    private Boolean inquiry;

    public NotificationParameters() {
        this.notice = false;
        this.invite = false;
        this.share = false;
        this.record = false;
        this.inquiry = false;
    }

    public void setAllTrue() {
        this.notice = true;
        this.invite = true;
        this.share = true;
        this.record = true;
        this.inquiry = true;
    }
}
