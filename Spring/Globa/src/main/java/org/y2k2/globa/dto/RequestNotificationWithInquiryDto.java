package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.entity.InquiryEntity;
import org.y2k2.globa.entity.UserEntity;

@Getter
@Setter
@AllArgsConstructor
public class RequestNotificationWithInquiryDto {
    private UserEntity fromUser;
    private UserEntity toUser;
    private InquiryEntity inquiry;
}
