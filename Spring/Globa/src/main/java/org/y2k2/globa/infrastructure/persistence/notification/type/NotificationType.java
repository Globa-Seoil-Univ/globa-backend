package org.y2k2.globa.infrastructure.persistence.notification.type;

import lombok.Getter;

@Getter
public enum NotificationType {
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

    NotificationType(char typeId) {
        this.typeId = typeId;
    }

    public String toStringType() {
        return String.valueOf(typeId);
    }

    public static char fromValue(String value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.name().equals(value)) {
                return type.typeId;
            }
        }
        throw new IllegalArgumentException("Invalid NotificationType value: " + value);
    }
}
