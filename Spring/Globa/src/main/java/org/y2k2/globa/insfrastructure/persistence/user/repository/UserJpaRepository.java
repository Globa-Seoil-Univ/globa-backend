package org.y2k2.globa.insfrastructure.persistence.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByCode(String code);

    Optional<UserEntity> findBySnsId(String snsId);

    Optional<UserEntity> findOneByCode(String code);

    Optional<UserEntity> findByUserId(Long userId);

    List<UserEntity> findAllByCodeIn(List<String> code);
}

