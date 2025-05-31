package org.y2k2.globa.fixture.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Import(QuizTestRepositoryImpl.class)
@Component
public class QuizFixture implements Fixture<QuizEntity> {
    @Autowired
    private QuizTestRepositoryImpl quizRepository;

    @Override
    public QuizEntity save(QuizEntity entity) {
        return quizRepository.save(entity);
    }

    public static QuizBuilder builder() {
        return new QuizBuilder();
    }

    public static class QuizBuilder {
        private RecordEntity record;

        private QuizBuilder() {}

        public QuizBuilder record(RecordEntity record) {
            this.record = record;
            return this;
        }

        public QuizEntity build() {
            QuizEntity quiz = new QuizEntity();
            quiz.setQuestion("Is it the capital of France?");
            quiz.setAnswer(true);
            quiz.setRecord(record);
            return quiz;
        }
    }
}
