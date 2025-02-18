package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findBySnsId(String snsId);

    Optional<UserEntity> findOneByCode(String code);

    Optional<UserEntity> findByUserId(Long userId);

    List<UserEntity> findAllByCodeIn(List<String> code);
}

