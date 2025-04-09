package org.y2k2.globa.fixture.quizattempt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.quizattempt.QuizAttemptFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Component
public class QuizAttemptFixture extends AbstractFixture<QuizAttemptEntity> {
    @Autowired
    private QuizAttemptFactory quizAttemptFactory;

    @Override
    protected QuizAttemptEntity build() {
        return quizAttemptFactory.createAndSave();
    }

    public QuizAttemptFixture withUser(UserEntity user) {
        quizAttemptFactory.setUser(user);
        return this;
    }

    public QuizAttemptFixture withQuiz(QuizEntity quiz) {
        quizAttemptFactory.setQuiz(quiz);
        return this;
    }

    public QuizAttemptFixture withIsCorrect(boolean isCorrect) {
        quizAttemptFactory.setIsCorrect(isCorrect);
        return this;
    }

    public QuizAttemptFixture withCreatedTime(LocalDateTime createdTime) {
        quizAttemptFactory.setCreatedTime(createdTime);
        return this;
    }

    public void update(QuizAttemptEntity entity) {
        quizAttemptFactory.updateAndSave(entity);
    }
}
