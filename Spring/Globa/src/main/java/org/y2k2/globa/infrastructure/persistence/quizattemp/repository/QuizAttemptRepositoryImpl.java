package org.y2k2.globa.infrastructure.persistence.quizattemp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class QuizAttemptRepositoryImpl implements QuizAttemptRepository {
    private final QuizAttemptJpaRepository quizAttemptJpaRepository;

    @Override
    public void saveAll(List<QuizAttemptEntity> entities) {
        quizAttemptJpaRepository.saveAll(entities);
    }
}
