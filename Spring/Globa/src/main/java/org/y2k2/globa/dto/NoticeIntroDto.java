package org.y2k2.globa.dto;

import lombok.Getter;
import org.y2k2.globa.entity.NoticeEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
public class NoticeIntroDto {
    private final Long noticeId;
    private final String thumbnail;
    private final String bgColor;

    public NoticeIntroDto(NoticeEntity notice) {
        this.noticeId = notice.getNoticeId();
        this.thumbnail = notice.getThumbnail();
        this.bgColor = notice.getBgColor();
    }

    public static List<NoticeIntroDto> fromArray(List<NoticeEntity> notices) {
        List<NoticeIntroDto> introDtoList = new ArrayList<>();

        notices.forEach(noticeEntity -> {
            introDtoList.add(new NoticeIntroDto(noticeEntity));
        });

        return introDtoList;
    }
}
