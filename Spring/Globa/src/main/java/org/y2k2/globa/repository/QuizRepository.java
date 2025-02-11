package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.entity.QuizEntity;
import org.y2k2.globa.entity.RecordEntity;

import java.util.List;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {

    QuizEntity findQuizEntityByQuizId(Long quizId);
    List<QuizEntity> findAllByRecordRecordId(Long recordId);
    List<QuizEntity> findAllByRecord(RecordEntity record);

    @Query(value = "SELECT " +
            "(SUM(is_correct) / COUNT(is_correct)) * 100 AS quizGrade, " +
            "DATE(created_time) AS createdTime " +
            "FROM quiz_attempt " +
            "WHERE user_id = :userId " +
            "AND is_correct IS NOT NULL " +
            "AND created_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +  // 최근 7일 제한 추가
            "GROUP BY DATE(created_time)", nativeQuery = true)
    List<QuizGradeProjection> findQuizGradeByUserInWeek(@Param("userId") Long userId);
}
