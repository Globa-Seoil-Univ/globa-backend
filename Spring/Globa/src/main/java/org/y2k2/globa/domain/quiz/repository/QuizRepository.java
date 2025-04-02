package org.y2k2.globa.domain.quiz.repository;

import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.List;

public interface QuizRepository {
    QuizEntity save(QuizEntity entity);
    void deleteAll(List<QuizEntity> entities);

    List<QuizEntity> getAllQuizzes(RecordEntity record);
    List<QuizEntity> getAllQuizzes(Long recordId);
    List<QuizEntity> getAllByQuizzesInRecord(RecordEntity record, List<Long> quizIds);
}
