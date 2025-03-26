package org.y2k2.globa.insfrastructure.persistence.folder.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.insfrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.domain.folder.FolderRepository;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class FolderRepositoryImpl implements FolderRepository {
    private final FolderJpaRepository folderJpaRepository;

    @Override
    public FolderEntity save(FolderEntity folder) {
        return folderJpaRepository.save(folder);
    }

    @Override
    public Optional<FolderEntity> findByFolderId(Long folderId) {
        return folderJpaRepository.findById(folderId);
    }

    @Override
    public Optional<FolderEntity> findByDefaultFolder(UserEntity user) {
        return folderJpaRepository.findByDefaultFolder(user);
    }

    @Override
    public Optional<FolderEntity> findByFolderIdWithoutDefault(Long folderId, UserEntity user) {
        return folderJpaRepository.findByFolderIdWithoutDefault(folderId, user);
    }

    @Override
    public Optional<FolderEntity> findByUserUserIdOrderByCreatedTimeAsc(Long userId) {
        return folderJpaRepository.findByUserUserIdOrderByCreatedTimeAsc(userId);
    }
}
