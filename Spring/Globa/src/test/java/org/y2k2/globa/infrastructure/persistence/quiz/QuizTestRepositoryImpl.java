package org.y2k2.globa.infrastructure.persistence.quiz;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizJpaRepository;
import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizRepositoryImpl;

@Component
@Primary
public class QuizTestRepositoryImpl extends QuizRepositoryImpl {
    private final QuizJpaRepository quizRepository;

    public QuizTestRepositoryImpl(QuizJpaRepository quizJpaRepository, QuizJpaRepository quizRepository) {
        super(quizJpaRepository);
        this.quizRepository = quizRepository;
    }

    public QuizEntity save(QuizEntity entity) {
        return quizRepository.save(entity);
    }
}
