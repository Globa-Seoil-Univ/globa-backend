package org.y2k2.globa.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Constant {
    JWT_HEADER("Authorization"),

    USER_PREFIX("/user"),
    FOLDER_PREFIX("/folder"),
    RECORD_PREFIX("/folder/{folderId}/record"),
    FOLDER_SHARE_PREFIX("/folder/{folderId}/share")
    ;

    private final String value;
}
