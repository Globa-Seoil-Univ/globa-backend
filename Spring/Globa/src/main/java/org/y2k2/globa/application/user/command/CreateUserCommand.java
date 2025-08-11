package org.y2k2.globa.application.user.command;

import org.y2k2.globa.application.user.dto.request.RequestUserPostDTO;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

public record CreateUserCommand(
        String code,
        SnsKind snsKind,
        String snsId,
        String name,
        String token,
        String profile,
        boolean notification,
        boolean eventNotification
) {
    public static CreateUserCommand from(RequestUserPostDTO dto, String code) {
        return new CreateUserCommand(
                code,
                SnsKind.fromCode(SnsKind.valueOf(dto.snsKind()).getCode()),
                dto.snsId(),
                dto.name(),
                dto.token(),
                dto.profile(),
                dto.notification() != null ? dto.notification() : false,
                dto.eventNotification() != null ? dto.eventNotification() : false
        );
    }
}
