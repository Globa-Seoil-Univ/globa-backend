package org.y2k2.globa.infrastructure.persistence.folderrole.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FolderRole {
    OWNER,
    READER,
    EDITOR,
    ;

    public static FolderRole from(String roleName) {
        for (FolderRole folderRole : values()) {
            if (folderRole.toString().equalsIgnoreCase(roleName)) {
                return folderRole;
            }
        }

        return READER;
    }
}
