package org.y2k2.globa.application.notice.dto.response;

import lombok.Getter;

public record ResponseNoticeDetailDto(
        String title,
        String content,
        String createdTime
) {
}
