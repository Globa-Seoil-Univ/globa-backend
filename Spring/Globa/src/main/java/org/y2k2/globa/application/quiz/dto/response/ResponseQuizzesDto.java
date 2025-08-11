package org.y2k2.globa.application.quiz.dto.response;

import org.y2k2.globa.application.quiz.dto.common.QuizDto;

import java.util.List;

public record ResponseQuizzesDto(
    List<QuizDto> quizzes
) {
}
