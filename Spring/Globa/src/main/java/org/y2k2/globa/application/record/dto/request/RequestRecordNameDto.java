package org.y2k2.globa.application.record.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RequestRecordNameDto(
        @NotBlank(message = "제목은 필수입니다.")
        String title
) {}
