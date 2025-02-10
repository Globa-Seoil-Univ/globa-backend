package org.y2k2.globa.dto.request.user;

import jakarta.validation.constraints.NotBlank;

public record RequestRTRDto(
        @NotBlank(message = "재갱신 토큰이 필수입니다.")
        String refreshToken
) {
}
