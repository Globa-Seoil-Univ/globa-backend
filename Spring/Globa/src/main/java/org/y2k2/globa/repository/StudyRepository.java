package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.entity.StudyEntity;
import org.y2k2.globa.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<StudyEntity, Long> {
    @Query(
            value = "SELECT * FROM study " +
                    "WHERE DATE(created_time) = DATE(NOW()) AND user_id = :userId AND record_id = :recordId",
            nativeQuery = true
    )
    Optional<StudyEntity> findByCreatedTime(@Param("userId") Long userId, @Param("recordId") Long recordId);
    List<StudyEntity> findAllByUserUserIdAndRecordRecordId(Long user,Long recordId);
}
