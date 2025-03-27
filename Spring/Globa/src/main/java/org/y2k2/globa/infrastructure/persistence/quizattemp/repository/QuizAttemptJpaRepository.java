package org.y2k2.globa.infrastructure.persistence.quizattemp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;

public interface QuizAttemptJpaRepository extends JpaRepository<QuizAttemptEntity, Long> {
}
