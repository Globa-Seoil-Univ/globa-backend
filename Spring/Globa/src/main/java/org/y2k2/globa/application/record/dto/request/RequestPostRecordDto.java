package org.y2k2.globa.application.record.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.y2k2.globa.common.annotation.EnumValue;
import org.y2k2.globa.infrastructure.persistence.record.type.Language;

public record RequestPostRecordDto(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 32, message = "제목은 32자 이내여야 합니다.")
        String title,
        @NotBlank(message = "파일 경로는 필수입니다.")
        String path,

        @NotNull
        @EnumValue(enumClass = Language.class, message = "언어는 KO, JA, EN 중 하나여야 합니다.")
        String lang
) {
}
