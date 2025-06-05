package org.y2k2.globa.application.answer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestAnswerDto(
        @NotBlank(message = "제목은 필수 입력 사항입니다.")
        String title,
        @NotBlank(message = "내용은 필수 입력 사항입니다.")
        @Size(max = 80, message = "최대 80자까지 입력 가능합니다.")
        String content
) {
}
