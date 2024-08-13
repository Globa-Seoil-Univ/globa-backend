package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.StudyEntity;
import org.y2k2.globa.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<StudyEntity, Long> {
    Optional<StudyEntity> findByCreatedTime(LocalDateTime createdTime);
    List<StudyEntity> findAllByUserUserId(Long user);
    List<StudyEntity> findAllByUserUserIdAndRecordRecordId(Long user,Long recordId);
}
