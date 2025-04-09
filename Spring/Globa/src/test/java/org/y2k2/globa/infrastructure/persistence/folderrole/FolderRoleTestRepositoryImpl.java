package org.y2k2.globa.infrastructure.persistence.folderrole;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleJpaRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleRepositoryImpl;

@Component
@Primary
public class FolderRoleTestRepositoryImpl extends FolderRoleRepositoryImpl {
    private final FolderRoleJpaRepository folderRoleRepository;

    public FolderRoleTestRepositoryImpl(FolderRoleJpaRepository folderRoleJpaRepository, FolderRoleJpaRepository folderRoleRepository) {
        super(folderRoleJpaRepository);
        this.folderRoleRepository = folderRoleRepository;
    }

    public FolderRoleEntity save(FolderRoleEntity entity) {
        return folderRoleRepository.save(entity);
    }
}
