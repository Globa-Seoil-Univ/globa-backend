package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.entity.UserRoleEntity;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
    Optional<UserRoleEntity> findByUser(UserEntity user);
}
