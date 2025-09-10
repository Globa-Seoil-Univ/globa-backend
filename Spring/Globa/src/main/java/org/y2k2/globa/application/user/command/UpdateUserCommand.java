package org.y2k2.globa.application.user.command;

import lombok.Builder;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Builder
public record UpdateUserCommand(
        UserEntity user,
        String name,
        FileDto profileImage,
        Boolean primaryNofi,
        Boolean uploadNofi,
        Boolean shareNofi,
        Boolean eventNofi
) {
}
