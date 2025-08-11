package org.y2k2.globa.application.notice.dto.response;

import java.util.List;

public record ResponseNoticeIntroDto(
        List<NoticeIntroDto> notices
) {
}
