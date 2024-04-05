package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.FolderRoleEntity;

public interface FolderRoleRepository extends JpaRepository<FolderRoleEntity, String> {
    public FolderRoleEntity findByRoleName(String roleName);
}
