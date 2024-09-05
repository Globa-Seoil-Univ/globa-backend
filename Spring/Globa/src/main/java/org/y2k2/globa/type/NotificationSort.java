package org.y2k2.globa.type;

import lombok.Getter;

@Getter
public enum NotificationSort {
    ALL("a"),
    NOTICE("n"),
    SHARE("s"),
    RECORD("r"),
    INQUIRY("i");

    private final String value;

    NotificationSort(String value) {
        this.value = value;
    }

    public static NotificationSort valueOfString(String s) {
        return switch (s) {
            case "n" -> NotificationSort.NOTICE;
            case "s" -> NotificationSort.SHARE;
            case "r" -> NotificationSort.RECORD;
            case "i" -> NotificationSort.INQUIRY;
            default -> NotificationSort.ALL;
        };
    }
}
