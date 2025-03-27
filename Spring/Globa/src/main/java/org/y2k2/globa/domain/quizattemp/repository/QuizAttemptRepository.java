package org.y2k2.globa.domain.quizattemp.repository;

import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;

import java.util.List;

public interface QuizAttemptRepository {
    void saveAll(List<QuizAttemptEntity> entities);
}
