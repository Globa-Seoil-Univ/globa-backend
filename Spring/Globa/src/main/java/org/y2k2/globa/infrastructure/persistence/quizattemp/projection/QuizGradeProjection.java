package org.y2k2.globa.infrastructure.persistence.quizattemp.projection;

import java.time.LocalDateTime;

public interface QuizGradeProjection {
    Double getQuizGrade();
    String getCreatedTime();
}