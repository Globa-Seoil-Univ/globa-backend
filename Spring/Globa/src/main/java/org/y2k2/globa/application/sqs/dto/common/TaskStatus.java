package org.y2k2.globa.application.sqs.dto.common;

public enum TaskStatus {
    SUCCESS,
    FAILED,
    NOT_STARTED,
    UNKNOWN,
    ;

    public static TaskStatus from(String status) {
        try {
            return TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
