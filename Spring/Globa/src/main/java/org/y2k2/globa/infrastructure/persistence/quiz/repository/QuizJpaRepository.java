package org.y2k2.globa.infrastructure.persistence.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.List;

public interface QuizJpaRepository extends JpaRepository<QuizEntity, Long> {
    List<QuizEntity> findAllByRecordRecordId(Long recordId);
    List<QuizEntity> findAllByRecordAndQuizIdIn(RecordEntity record, List<Long> quizIds);
}
