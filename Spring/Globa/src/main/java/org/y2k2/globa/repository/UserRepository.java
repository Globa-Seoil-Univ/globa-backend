package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.Projection.StudyTimeProjection;
import org.y2k2.globa.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findBySnsId(String snsId);
    UserEntity findOneByUserId(Long userId);
    Optional<UserEntity> findOneByCode(String code);
    List<UserEntity> findAllByCodeIn(List<String> code);
    UserEntity findByUserId(Long userId);
    Page<UserEntity> findAll(Pageable pageable);
}

