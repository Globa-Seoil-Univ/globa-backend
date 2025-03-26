package org.y2k2.globa.application.foldershare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.application.user.dto.common.UserIntroDto;
import org.y2k2.globa.common.type.InvitationStatus;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseFolderShareUserDto {
    @Getter
    @AllArgsConstructor
    public static class FolderShareUserDto {
        private final Long shareId;
        private final String roleId;
        private final UserIntroDto user;
        private final InvitationStatus invitationStatus;
    }

    private final List<FolderShareUserDto> users;
    private Long total;
}
