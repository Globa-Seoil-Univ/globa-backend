package org.y2k2.globa.application.record.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestRecordNameDto(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 32, message = "제목은 최대 32자까지 입력할 수 있습니다.")
        String title
) {}
