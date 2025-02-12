package org.y2k2.globa.dto.request.user;

import jakarta.validation.constraints.NotBlank;

public record RequestNameDto(
        @NotBlank(message = "이름은 필수입니다.")
        String name
) {}
