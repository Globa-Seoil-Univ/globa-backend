package org.y2k2.globa.application.answer.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RequestAnswerDto(
        @NotBlank(message = "You must request title field")
        String title,
        @NotBlank(message = "You must request content field")
        String content
) {
}
