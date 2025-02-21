package org.y2k2.globa.dto.request.foldershare;

import org.y2k2.globa.annotation.EnumValue;
import org.y2k2.globa.type.Role;

public record RequestInviteDto(
        @EnumValue(enumClass = Role.class, message = "권한은 READER 또는 WRITER만 가능합니다.")
        String role
) {}
