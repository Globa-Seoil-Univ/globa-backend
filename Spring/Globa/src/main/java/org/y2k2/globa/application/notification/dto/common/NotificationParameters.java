package org.y2k2.globa.application.notification.dto.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NotificationParameters {
    private Boolean notice = false;
    private Boolean invite = false;
    private Boolean share = false;
    private Boolean record = false;
    private Boolean inquiry = false;

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
