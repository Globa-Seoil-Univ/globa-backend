package org.y2k2.globa.application.sqs.dto.common;

public enum TaskType {
    OPENAI_API_ERROR,
    JSON_PARSING_ERROR,
    DATABASE_ERROR,
    VALIDATION_ERROR,
    UNKNOWN_ERROR;

    public static TaskType from(String type) {
        try {
            return TaskType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN_ERROR;
        }
    }
}
