package org.y2k2.globa.application.user.dto.response;

public record ResponseNotificationSettingDto(
        Boolean uploadNofi,
        Boolean shareNofi,
        Boolean eventNofi
) {}