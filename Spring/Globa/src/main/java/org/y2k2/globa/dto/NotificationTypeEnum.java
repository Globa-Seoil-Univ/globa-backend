package org.y2k2.globa.dto;

public enum NotificationTypeEnum {
    NOTICE('1'),
    SHARE_FOLDER_INVITE('2'),
    SHARE_FOLDER_ADD_FILE('3'),
    SHARE_FOLDER_ADD_USER('4'),
    SHARE_FOLDER_ADD_COMMENT('5'),
    UPLOAD_SUCCESS('6'),
    UPLOAD_FAILED('7'),
    INQUIRY('8'),
    ;

    private final char typeId;

    NotificationTypeEnum(char typeId) {
        this.typeId = typeId;
    }

    public char getTypeId() {
        return typeId;
    }
}
