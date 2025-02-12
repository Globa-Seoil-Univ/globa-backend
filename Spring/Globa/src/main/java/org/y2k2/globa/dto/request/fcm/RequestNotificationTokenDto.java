package org.y2k2.globa.dto.request.fcm;

import jakarta.validation.constraints.NotBlank;

public record RequestNotificationTokenDto(
        @NotBlank(message = "FCM 토큰은 필수입니다.")
        String token
) {}
