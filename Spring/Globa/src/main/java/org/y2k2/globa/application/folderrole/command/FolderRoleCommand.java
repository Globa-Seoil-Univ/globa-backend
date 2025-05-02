package org.y2k2.globa.application.folderrole.command;

import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;

public record FolderRoleCommand(
        FolderRole folderRole
) {
    public static FolderRoleCommand of(FolderRole folderRole) {
        return new FolderRoleCommand(folderRole);
    }
}
