package org.y2k2.globa.application.user.dto.response;

public record ResponseNotificationSettingDto(
        Boolean primaryNofi,
        Boolean uploadNofi,
        Boolean shareNofi,
        Boolean eventNofi
) {}