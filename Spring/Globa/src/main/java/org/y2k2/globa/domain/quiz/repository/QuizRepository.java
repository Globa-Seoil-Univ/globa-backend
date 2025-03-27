package org.y2k2.globa.domain.quiz.repository;

import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.projection.QuizGradeProjection;

import java.util.List;

public interface QuizRepository {
    void deleteAll(List<QuizEntity> entities);

    List<QuizEntity> getAllQuizzes(RecordEntity record);
    List<QuizEntity> getAllQuizzes(Long recordId);
    List<QuizEntity> getAllByQuizzesInRecord(RecordEntity record, List<Long> quizIds);
    List<QuizGradeProjection> getQuizInWeek(Long userId);
    List<QuizGradeProjection> getQuizByUserAndRecord(UserEntity user, Long recordId);
}
