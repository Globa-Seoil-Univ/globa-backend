package org.y2k2.globa.dto.request.notification;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.dto.common.fcm.SendMessage;
import org.y2k2.globa.entity.InquiryEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithInquiryDto extends SendMessage {
    private InquiryEntity inquiry;
}
