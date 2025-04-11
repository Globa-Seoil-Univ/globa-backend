package org.y2k2.globa.infrastructure.persistence.userrole.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

import java.util.List;
import java.util.Optional;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, Long> {
    Boolean existsByUser_UserIdAndRole_NameIn(Long userId, List<UserRole> roleName);

    Optional<UserRoleEntity> findByUser_UserId(Long userId);
}
