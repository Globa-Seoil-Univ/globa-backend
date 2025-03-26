package org.y2k2.globa.insfrastructure.persistence.folderrole.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.insfrastructure.persistence.folderrole.entity.FolderRoleEntity;

import java.util.Optional;

public interface FolderRoleJpaRepository extends JpaRepository<FolderRoleEntity, String> {
    Optional<FolderRoleEntity> findByRoleName(String roleName);
}
