package org.y2k2.globa.infrastructure.persistence.quiz.projection;

import java.time.LocalDateTime;

public interface QuizGradeProjection {
    Double getQuizGrade();
    LocalDateTime getCreatedTime();
}