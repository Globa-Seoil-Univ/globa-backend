package org.y2k2.globa.infrastructure.persistence.folderrole.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;

import java.util.Optional;

public interface FolderRoleJpaRepository extends JpaRepository<FolderRoleEntity, String> {
    Optional<FolderRoleEntity> findByRoleName(FolderRole roleName);
}
