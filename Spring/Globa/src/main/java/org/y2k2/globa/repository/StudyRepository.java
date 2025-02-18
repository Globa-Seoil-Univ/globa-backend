package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.Projection.StudyTimeProjection;
import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.StudyEntity;
import org.y2k2.globa.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<StudyEntity, Long> {
    @Query(
            value = "SELECT s FROM StudyEntity s " +
                    "WHERE s.user = :user AND s.record = :record " +
                        "AND DATE(s.createdTime) = DATE(NOW())"
    )
    Optional<StudyEntity> findByCreatedTime(UserEntity user, RecordEntity record);
    List<StudyEntity> findAllByUserAndRecordRecordId(UserEntity user, Long recordId);

    @Query(value = "SELECT " +
            "SUM(study_time) AS totalStudyTime, " + // 각 날짜별 StudyTime 합산
            "DATE(created_time) AS createdTime " +   // 날짜별 그룹핑
            "FROM study " +
            "WHERE user_id = :userId " +
            "AND created_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +  // 최근 7일 이내 데이터 필터링
            "GROUP BY DATE(created_time)", nativeQuery = true)  // 날짜별로 그룹핑
    List<StudyTimeProjection> findStudyTimeByUserInWeek(@Param("userId") Long userId);
}
