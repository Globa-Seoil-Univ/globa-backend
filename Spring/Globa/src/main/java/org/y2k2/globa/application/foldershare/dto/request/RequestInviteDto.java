package org.y2k2.globa.application.foldershare.dto.request;

import org.y2k2.globa.common.annotation.EnumValue;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;

public record RequestInviteDto(
        @EnumValue(enumClass = FolderRole.class, message = "권한은 READER 또는 WRITER만 가능합니다.")
        String role
) {}
