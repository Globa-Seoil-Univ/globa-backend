package org.y2k2.globa.infrastructure.persistence.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.study.projection.StudyTimeProjection;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyJpaRepository extends JpaRepository<StudyEntity, Long> {
    List<StudyEntity> findAllByUserAndRecordRecordId(UserEntity user, Long recordId);

    @Query(value = "SELECT " +
            "SUM(s.studyTime) AS totalStudyTime, " +
            "DATE(s.createdTime) AS createdTime " +
            "FROM StudyEntity s " +
            "WHERE s.user.userId = :userId " +
            "AND s.createdTime >= :intervalDay " +
            "GROUP BY DATE(s.createdTime)")
    List<StudyTimeProjection> findStudyTimeByInDays(
            @Param("userId") Long userId,
            @Param("intervalDay") LocalDateTime intervalDay
    );

    @Query(
            value = "SELECT s FROM StudyEntity s " +
                    "WHERE s.user = :user AND s.record = :record " +
                    "AND DATE(s.createdTime) = DATE(NOW())"
    )
    Optional<StudyEntity> findByCreatedTime(UserEntity user, RecordEntity record);
}
