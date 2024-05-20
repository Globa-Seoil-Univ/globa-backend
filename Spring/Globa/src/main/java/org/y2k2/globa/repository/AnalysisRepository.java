package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.entity.AnalysisEntity;
import org.y2k2.globa.entity.SectionEntity;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {
    AnalysisEntity findAllBySectionSectionId(Long sectionId);

    @Query(value = "SELECT " +
            "(SUM(qa.is_correct) / COUNT(qa.is_correct)) * 100 AS quizGrade, " +
            "DATE(qa.created_time) AS createdTime " +
            "FROM quiz_attempt qa " +
            "JOIN quiz q ON qa.quiz_id = q.quiz_id " +
            "WHERE qa.user_id = :userId " +
            "AND qa.is_correct IS NOT NULL " +
            "AND q.record_id = :recordId " +
            "GROUP BY DATE(qa.created_time);", nativeQuery = true)
    List<QuizGradeProjection> findQuizGradeByUserUserIdAndRecordRecordId(@Param("userId") Long userId, Long recordId);

    @Query(value = "SELECT word, importance " +
            "FROM keyword " +
            "WHERE record_id = :recordId " +
            "ORDER BY importance DESC " +
            "LIMIT 10; ", nativeQuery = true)
    List<KeywordProjection> findKeywordByRecordId(@Param("recordId") Long recordId);

    List<AnalysisEntity> findAllBySection(SectionEntity section);
}
