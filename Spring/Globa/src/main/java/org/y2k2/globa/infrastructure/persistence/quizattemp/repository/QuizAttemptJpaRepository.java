package org.y2k2.globa.infrastructure.persistence.quizattemp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.infrastructure.persistence.quizattemp.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizAttemptJpaRepository extends JpaRepository<QuizAttemptEntity, Long> {
    @Query(
            value = "SELECT " +
                    "(SUM(CASE WHEN qa.isCorrect = TRUE THEN 1 ELSE 0 END) / COUNT(qa.isCorrect)) * 100 AS quizGrade, " +
                    "DATE(qa.createdTime) AS createdTime " +
                    "FROM QuizAttemptEntity qa " +
                    "WHERE qa.user.userId = :userId " +
                    "AND qa.isCorrect IS NOT NULL " +
                    "AND qa.createdTime >= :intervalDay " +
                    "GROUP BY DATE(qa.createdTime)"
    )
    List<QuizGradeProjection> findQuizGradeByUserInDays(
            @Param("userId") Long userId,
            @Param("intervalDay") LocalDateTime intervalDay
    );

    @Query(
            value = "SELECT (SUM(CASE WHEN qa.isCorrect THEN 1 ELSE 0 END) / COUNT(qa.isCorrect)) * 100 AS quizGrade" +
                    ", qa.createdTime AS createdTime " +
                    "FROM QuizAttemptEntity qa " +
                    "JOIN qa.quiz q ON qa.quiz = q " +
                    "WHERE qa.user = :user " +
                    "AND qa.isCorrect IS NOT NULL " +
                    "AND q.record.recordId = :recordId " +
                    "GROUP BY DATE(qa.createdTime)"
    )
    List<QuizGradeProjection> findQuizGradeByUserAndRecordId(UserEntity user, Long recordId);
}
