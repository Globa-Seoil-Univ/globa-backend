package org.y2k2.globa.infrastructure.persistence.quiz.projection;

import org.y2k2.globa.infrastructure.persistence.quizattemp.projection.QuizGradeProjection;

public class QuizGradeProjectionImpl implements QuizGradeProjection {
    Double quizGrade;
    String createdTime;

    public QuizGradeProjectionImpl(Double quizGrade, String createdTime) {
        this.quizGrade = quizGrade;
        this.createdTime = createdTime;
    }

    @Override
    public Double getQuizGrade() {
        return quizGrade;
    }

    @Override
    public String getCreatedTime() {
        return createdTime;
    }
}
