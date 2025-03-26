package org.y2k2.globa.insfrastructure.persistence.folderrole.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.insfrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.domain.folderrole.FolderRoleRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class FolderRoleRepositoryImpl implements FolderRoleRepository {
    private final FolderRoleJpaRepository folderRoleJpaRepository;

    @Override
    public FolderRoleEntity save(FolderRoleEntity entity) {
        return folderRoleJpaRepository.save(entity);
    }

    @Override
    public Optional<FolderRoleEntity> findByRoleName(String roleName) {
        return folderRoleJpaRepository.findByRoleName(roleName);
    }
}
