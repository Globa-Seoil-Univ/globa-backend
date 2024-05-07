package org.y2k2.globa.dto;

import lombok.Getter;

@Getter
public class NoticeIntroResponseDto {
    private final Long noticeId;
    private final String thumbnail;
    private final String bgColor;

    public NoticeIntroResponseDto(Long noticeId, String thumbnail, String bgColor) {
        this.noticeId = noticeId;
        this.thumbnail = thumbnail;
        this.bgColor = bgColor;
    }
}

