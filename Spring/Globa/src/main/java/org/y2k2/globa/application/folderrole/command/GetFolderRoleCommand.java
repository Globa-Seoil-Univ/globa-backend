package org.y2k2.globa.application.folderrole.command;

import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;

public record GetFolderRoleCommand(
        FolderRole folderRole
) {
    public static GetFolderRoleCommand of(FolderRole folderRole) {
        return new GetFolderRoleCommand(folderRole);
    }
}
