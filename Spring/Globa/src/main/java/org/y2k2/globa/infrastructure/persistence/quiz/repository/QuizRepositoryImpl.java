package org.y2k2.globa.infrastructure.persistence.quiz.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class QuizRepositoryImpl implements QuizRepository {
    private final QuizJpaRepository quizJpaRepository;

    @Override
    public void deleteAll(List<QuizEntity> entities) {
        quizJpaRepository.deleteAllInBatch(entities);
    }

    @Override
    public List<QuizEntity> getAllQuizzes(RecordEntity record) {
        return quizJpaRepository.findAllByRecord(record);
    }

    @Override
    public List<QuizEntity> getAllQuizzes(Long recordId) {
        return quizJpaRepository.findAllByRecordRecordId(recordId);
    }

    @Override
    public List<QuizEntity> getAllByQuizzesInRecord(RecordEntity record, List<Long> quizIds) {
        return quizJpaRepository.findAllByRecordAndQuizIdIn(record, quizIds);
    }

    @Override
    public List<QuizGradeProjection> getQuizInWeek(Long userId) {
        return quizJpaRepository.findQuizGradeByUserInWeek(userId);
    }

    @Override
    public List<QuizGradeProjection> getQuizByUserAndRecord(UserEntity user, Long recordId) {
        return quizJpaRepository.findQuizGradeByUserAndRecordRecordId(user, recordId);
    }
}
