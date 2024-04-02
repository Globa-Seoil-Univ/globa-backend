package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FolderShareUserDto {
    private final String roleId;
    private final UserIntroDto user;
    private final InvitationStatus invitationStatus;
}

