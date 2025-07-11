package org.y2k2.globa.application.kafka.dto.common;

public record ErrorStatus(
        TaskStatus addSection,
        TaskStatus assignText,
        TaskStatus addSummary,
        TaskStatus addQa,
        TaskStatus addKeywords
) {
}
