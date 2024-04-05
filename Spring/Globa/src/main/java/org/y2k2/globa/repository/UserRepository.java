package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.dto.ResponseQuizGradeDto;
import org.y2k2.globa.entity.UserEntity;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findBySnsId(String snsId);
    UserEntity findOneByUserId(Long userId);
    UserEntity findOneByCode(String code);
    UserEntity findByUserId(Long userId);

    Page<UserEntity> findAll(Pageable pageable);
//    Page<RankingEntity> findRankingEntities (Pageable pageable);
    
    @Query(value = "SELECT " +
        "(SUM(is_correct) / COUNT(is_correct)) * 100 AS quizGrade, " +
        "DATE(created_time) AS createdTime " +
        "FROM quiz_attempt " +
        "WHERE user_id = :userId " +
        "AND is_correct IS NOT NULL " +
        "GROUP BY DATE(created_time)", nativeQuery = true)
    List<QuizGradeProjection> findQuizGradeByUser(@Param("userId") Long userId);

    @Query(value = "SELECT word, importance " +
            "FROM keyword " +
            "WHERE record_id = :recordId " +
            "ORDER BY importance DESC " +
            "LIMIT 10 ", nativeQuery = true)
    List<KeywordProjection> findKeywordByRecordId(@Param("recordId") Long recordId);
}

