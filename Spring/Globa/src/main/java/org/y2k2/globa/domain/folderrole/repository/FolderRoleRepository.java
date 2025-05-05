package org.y2k2.globa.domain.folderrole.repository;

import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;

import java.util.Optional;

public interface FolderRoleRepository {
    Optional<FolderRoleEntity> getRole(FolderRole roleName);
}
