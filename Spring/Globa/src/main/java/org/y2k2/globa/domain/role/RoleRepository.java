package org.y2k2.globa.domain.role;

import org.y2k2.globa.insfrastructure.persistence.role.entity.RoleEntity;

import java.util.Optional;

public interface RoleRepository {
    Optional<RoleEntity> findByName(String name);
}
