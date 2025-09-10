package org.y2k2.globa.application.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record RequestNotificationSettingDto(
        @NotNull(message = "primaryNofi는 필수입니다.")
        Boolean primaryNofi,
        @NotNull(message = "uploadNofi는 필수입니다.")
        Boolean uploadNofi,
        @NotNull(message = "shareNofi는 필수입니다.")
        Boolean shareNofi,
        @NotNull(message = "eventNofi는 필수입니다.")
        Boolean eventNofi
) {}