package org.y2k2.globa.domain.folderrole.repository;

import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;

import java.util.Optional;

public interface FolderRoleRepository {
    FolderRoleEntity save(FolderRoleEntity folderRoleEntity);
    Optional<FolderRoleEntity> getRole(FolderRole roleName);
}
