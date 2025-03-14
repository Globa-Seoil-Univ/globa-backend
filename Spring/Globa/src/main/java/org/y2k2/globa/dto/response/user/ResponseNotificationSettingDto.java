package org.y2k2.globa.dto.response.user;

import lombok.Builder;

@Builder
public record ResponseNotificationSettingDto(
        Boolean uploadNofi,
        Boolean shareNofi,
        Boolean eventNofi
) {}