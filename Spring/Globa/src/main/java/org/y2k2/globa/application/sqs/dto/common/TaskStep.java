package org.y2k2.globa.application.sqs.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TaskStep {
    STT,
    ADD_SECTION,
    ASSIGN_TEXT,
    ADD_SUMMARY,
    ADD_QA,
    ADD_KEYWORDS,
    UNKNOWN,
    ;

    @JsonCreator
    public static TaskStep from(String step) {
        try {
            return TaskStep.valueOf(step.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
