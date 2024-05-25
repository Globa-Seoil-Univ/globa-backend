package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.entity.UserEntity;

@Getter
@Setter
@AllArgsConstructor
public class RequestNotificationWithNoticeDto {
    private UserEntity fromUser;
    private NoticeEntity notice;
}
