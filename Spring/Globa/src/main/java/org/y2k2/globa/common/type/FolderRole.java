package org.y2k2.globa.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FolderRole {
    OWNER("소유자"),
    READER("뷰어"),
    WRITER("편집자"),
    ;

    private final String roleName;

    public static FolderRole from(String roleName) {
        for (FolderRole folderRole : values()) {
            if (folderRole.toString().equalsIgnoreCase(roleName)) {
                return folderRole;
            }
        }

        return READER;
    }
}
