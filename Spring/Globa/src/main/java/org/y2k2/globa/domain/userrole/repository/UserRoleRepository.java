package org.y2k2.globa.domain.userrole.repository;

import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

public interface UserRoleRepository {
    void save(UserRoleEntity entity);

    Boolean isWritable(Long userId);

    Optional<UserRoleEntity> getUserRole(Long userId);
}
