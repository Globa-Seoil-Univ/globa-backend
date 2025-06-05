package org.y2k2.globa.application.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestInquiryDto(
        @NotBlank(message = "You must request title field")
        @Size(max = 80, message = "제목은 최대 80자까지 입력할 수 있습니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력 사항입니다.")
        String content
) {}
