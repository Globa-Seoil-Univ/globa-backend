package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.QuizAttemptEntity;

public interface QuizAttemptRepository extends JpaRepository<QuizAttemptEntity, Long> {
}
