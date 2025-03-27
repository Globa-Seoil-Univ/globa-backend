package org.y2k2.globa.application.notification.dto.common;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;

@Getter
@SuperBuilder
public class RequestNotificationWithInquiryDto extends SendMessage {
    private InquiryEntity inquiry;
}
