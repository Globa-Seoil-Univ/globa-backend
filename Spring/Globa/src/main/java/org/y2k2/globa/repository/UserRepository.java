package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.Projection.StudyTimeProjection;
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
            "SUM(study_time) AS totalStudyTime, " + // 각 날짜별 StudyTime 합산
            "DATE(created_time) AS createdDate " +   // 날짜별 그룹핑
            "FROM study " +
            "WHERE user_id = :userId " +
            "AND created_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +  // 최근 7일 이내 데이터 필터링
            "GROUP BY DATE(created_time)", nativeQuery = true)  // 날짜별로 그룹핑
    List<StudyTimeProjection> findAllByUserUserId(@Param("userId") Long userId);

    @Query(value = "SELECT " +
        "(SUM(is_correct) / COUNT(is_correct)) * 100 AS quizGrade, " +
        "DATE(created_time) AS createdTime " +
        "FROM quiz_attempt " +
        "WHERE user_id = :userId " +
        "AND is_correct IS NOT NULL " +
        "AND created_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +  // 최근 7일 제한 추가
        "GROUP BY DATE(created_time)", nativeQuery = true)
    List<QuizGradeProjection> findQuizGradeByUser(@Param("userId") Long userId);

    @Query(value = "SELECT word, importance " +
            "FROM keyword " +
            "WHERE record_id = :recordId " +
            "ORDER BY importance DESC " +
            "LIMIT 10 ", nativeQuery = true)
    List<KeywordProjection> findKeywordByRecordId(@Param("recordId") Long recordId);

    @Query(value = "SELECT word, importance " +
            "FROM keyword " +
            "WHERE record_id IN (:recordIds) " +
            "ORDER BY importance DESC " +
            "LIMIT 10", nativeQuery = true)
    List<KeywordProjection> findKeywordByRecordIds(@Param("recordIds") List<Long> recordIds);
}

