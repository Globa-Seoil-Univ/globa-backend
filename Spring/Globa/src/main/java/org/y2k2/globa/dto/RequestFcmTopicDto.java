package org.y2k2.globa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor
public class RequestFcmTopicDto {
    @Schema(name = "title", description = "알림 제목", example = "테스트 알림 제목입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    @Schema(name = "body", description = "알림 내용", example = "테스트 알림 내용입니다", requiredMode = Schema.RequiredMode.REQUIRED)
    private String body;
    @Schema(name = "topic", description = "알림 주제 이름", example = "notice", requiredMode = Schema.RequiredMode.REQUIRED)
    private String topic;
}
