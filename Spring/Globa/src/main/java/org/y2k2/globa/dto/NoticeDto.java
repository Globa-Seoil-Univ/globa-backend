package org.y2k2.globa.dto;

import lombok.Getter;
import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.util.CustomTimestamp;

@Getter
public class NoticeDto {
    private final String title;
    private final String content;
    private final String createdTime;

    public NoticeDto(NoticeEntity notice) {
        CustomTimestamp timestamp = new CustomTimestamp();
        timestamp.setTimestamp(notice.getCreatedTime());

        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.createdTime = String.valueOf(timestamp);
    }

    public static NoticeDto from(NoticeEntity notice) {
        return new NoticeDto(notice);
    }
}
