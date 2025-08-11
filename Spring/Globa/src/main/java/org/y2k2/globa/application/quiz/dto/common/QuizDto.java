package org.y2k2.globa.application.quiz.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

public record QuizDto(
        Long quizId,
        String question,
        Boolean answer
) {
}
