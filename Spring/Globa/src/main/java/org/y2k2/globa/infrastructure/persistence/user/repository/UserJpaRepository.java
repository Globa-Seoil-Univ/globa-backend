package org.y2k2.globa.infrastructure.persistence.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByCode(String code);

    Optional<UserEntity> findBySnsId(String snsId);

    Optional<UserEntity> findByCode(String code);

    Optional<UserEntity> findByUserId(Long userId);

    List<UserEntity> findAllByCodeIn(List<String> code);

    @Query("SELECT u FROM UserEntity u WHERE u.isDeleted = true AND u.deletedTime <= :intervalDay")
    List<UserEntity> findAllByIsDeletedTrue(LocalDateTime intervalDay);
}

