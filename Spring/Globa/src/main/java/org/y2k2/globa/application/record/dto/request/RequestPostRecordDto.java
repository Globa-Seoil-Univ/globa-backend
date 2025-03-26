package org.y2k2.globa.application.record.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RequestPostRecordDto(
        @NotBlank(message = "제목은 필수입니다.")
        String title,
        @NotBlank(message = "파일 경로는 필수입니다.")
        String path
) {
}
