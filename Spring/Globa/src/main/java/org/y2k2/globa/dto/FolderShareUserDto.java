package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.type.InvitationStatus;

@Getter
@AllArgsConstructor
public class FolderShareUserDto {
    private final long shareId;
    private final String roleId;
    private final UserIntroDto user;
    private final InvitationStatus invitationStatus;
}

