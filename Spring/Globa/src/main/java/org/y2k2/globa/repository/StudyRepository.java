package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.StudyEntity;
import org.y2k2.globa.entity.UserEntity;

import java.util.List;

public interface StudyRepository extends JpaRepository<StudyEntity, Long> {
    List<StudyEntity> findAllByUserUserId(Long user);
}
