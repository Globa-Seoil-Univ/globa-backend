package org.y2k2.globa.domain.folderrole;

import org.y2k2.globa.insfrastructure.persistence.folderrole.entity.FolderRoleEntity;

import java.util.Optional;

public interface FolderRoleRepository {
    FolderRoleEntity save(FolderRoleEntity entity);
    Optional<FolderRoleEntity> findByRoleName(String roleName);
}
