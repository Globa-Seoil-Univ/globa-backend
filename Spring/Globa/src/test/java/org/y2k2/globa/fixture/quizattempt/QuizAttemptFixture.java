package org.y2k2.globa.fixture.quizattempt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.repository.QuizAttemptRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Import(QuizAttemptRepositoryImpl.class)
@Component
public class QuizAttemptFixture implements Fixture<QuizAttemptEntity> {
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Override
    public QuizAttemptEntity save(QuizAttemptEntity entity) {
        return quizAttemptRepository.save(entity);
    }

    public static QuizAttemptBuilder builder() {
        return new QuizAttemptBuilder();
    }

    public static class QuizAttemptBuilder {
        private UserEntity user;
        private QuizEntity quiz;
        private boolean isCorrect = false;
        private LocalDateTime createdTime = new CustomTimestamp().getTimestamp();

        private QuizAttemptBuilder() {}

        public QuizAttemptBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public QuizAttemptBuilder quiz(QuizEntity quiz) {
            this.quiz = quiz;
            return this;
        }

        public QuizAttemptBuilder isCorrect(boolean isCorrect) {
            this.isCorrect = isCorrect;
            return this;
        }

        public QuizAttemptBuilder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public QuizAttemptEntity build() {
            QuizAttemptEntity entity = new QuizAttemptEntity();
            entity.setUser(user);
            entity.setQuiz(quiz);
            entity.setIsCorrect(isCorrect);
            entity.setCreatedTime(createdTime);
            return entity;
        }
    }
}
