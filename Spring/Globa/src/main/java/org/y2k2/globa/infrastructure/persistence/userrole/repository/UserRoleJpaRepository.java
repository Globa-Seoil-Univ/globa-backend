package org.y2k2.globa.infrastructure.persistence.userrole.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

import java.util.Optional;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, Long> {
    Optional<UserRoleEntity> findByUser(UserEntity user);
}
