package org.y2k2.globa.dto;

import lombok.Getter;

@Getter
public class NoticeDetailResponseDto {
    private final String title;
    private final String content;
    private final String createdTime;

    public NoticeDetailResponseDto(String title, String content, String createdTime) {
        this.title = title;
        this.content = content;
        this.createdTime = createdTime;
    }
}
