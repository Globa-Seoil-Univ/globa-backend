package org.y2k2.globa.application.study.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RequestStudyDto(
        @NotNull(message = "공부 시간은 필수입니다.")
        @Min(0)
        Long studyTime
) {}
