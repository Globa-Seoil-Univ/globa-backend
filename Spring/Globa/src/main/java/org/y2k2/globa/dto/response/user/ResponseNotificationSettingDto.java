package org.y2k2.globa.dto.response.user;

public record ResponseNotificationSettingDto(
        Boolean uploadNofi,
        Boolean shareNofi,
        Boolean eventNofi
) {}