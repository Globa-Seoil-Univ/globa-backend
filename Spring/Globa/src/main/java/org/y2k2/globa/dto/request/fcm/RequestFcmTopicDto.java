package org.y2k2.globa.dto.request.fcm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RequestFcmTopicDto(
        @NotBlank(message = "알림 제목은 필수 입력입니다.")
        @Schema(name = "title", description = "알림 제목", example = "테스트 알림 제목입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotBlank(message = "알림 내용은 필수 입력입니다.")
        @Schema(name = "body", description = "알림 내용", example = "테스트 알림 내용입니다", requiredMode = Schema.RequiredMode.REQUIRED)
        String body,

        @NotBlank(message = "알림 주제 이름은 필수 입력입니다.")
        @Schema(name = "topic", description = "알림 주제 이름", example = "notice", requiredMode = Schema.RequiredMode.REQUIRED)
        String topic
) {}
