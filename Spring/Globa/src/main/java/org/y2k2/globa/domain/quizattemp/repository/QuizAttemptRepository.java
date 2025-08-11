package org.y2k2.globa.domain.quizattemp.repository;

import org.y2k2.globa.infrastructure.persistence.quizattemp.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

public interface QuizAttemptRepository {
    QuizAttemptEntity save(QuizAttemptEntity entity);
    void saveAll(List<QuizAttemptEntity> entities);

    List<QuizGradeProjection> getQuizAttemptByUserInDays(Long userId);
    List<QuizGradeProjection> getQuizAttemptByUserAndRecordId(Long userId, Long recordId);
}
