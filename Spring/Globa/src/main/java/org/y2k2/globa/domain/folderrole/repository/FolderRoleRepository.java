package org.y2k2.globa.domain.folderrole.repository;

import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;

import java.util.Optional;

public interface FolderRoleRepository {
    FolderRoleEntity save(FolderRoleEntity folderRoleEntity);
    Optional<FolderRoleEntity> getRole(String roleName);
}
