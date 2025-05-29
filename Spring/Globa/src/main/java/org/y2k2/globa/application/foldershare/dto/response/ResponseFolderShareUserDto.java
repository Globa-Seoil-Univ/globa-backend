package org.y2k2.globa.application.foldershare.dto.response;

import org.y2k2.globa.application.user.dto.common.UserIntroDto;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;

import java.util.List;

public record ResponseFolderShareUserDto(List<FolderShareUserDto> users, Long total) {
    public record FolderShareUserDto(Long shareId, Long roleId, UserIntroDto user, InvitationStatus invitationStatus) {
    }
}
