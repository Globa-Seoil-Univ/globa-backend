package org.y2k2.globa.infrastructure.persistence.quizattemp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.infrastructure.persistence.quizattemp.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class QuizAttemptRepositoryImpl implements QuizAttemptRepository {
    private final QuizAttemptJpaRepository quizAttemptJpaRepository;

    @Override
    public QuizAttemptEntity save(QuizAttemptEntity entity) {
        return quizAttemptJpaRepository.save(entity);
    }

    @Override
    public void saveAll(List<QuizAttemptEntity> entities) {
        quizAttemptJpaRepository.saveAll(entities);
    }

    @Override
    public List<QuizGradeProjection> getQuizAttemptByUserInDays(Long userId) {
        LocalDateTime intervalDay = new CustomTimestamp().getTimestamp().minusDays(7);
        return quizAttemptJpaRepository.findQuizGradeByUserInDays(userId, intervalDay);
    }

    @Override
    public List<QuizGradeProjection> getQuizAttemptByUserAndRecordId(UserEntity user, Long recordId) {
        return quizAttemptJpaRepository.findQuizGradeByUserAndRecordId(user, recordId);
    }
}
