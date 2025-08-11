package org.y2k2.globa.application.fcm.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RequestNotificationTokenDto(
        @NotBlank(message = "FCM 토큰은 필수입니다.")
        String token
) {}
