package org.y2k2.globa.domain.role.repository;

import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;

import java.util.Optional;

public interface RoleRepository {
    Optional<RoleEntity> getRole(String name);
}
