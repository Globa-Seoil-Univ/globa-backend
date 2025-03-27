package org.y2k2.globa.infrastructure.persistence.folder.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;

import java.awt.print.Pageable;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class FolderRepositoryImpl implements FolderRepository {
    private final FolderJpaRepository folderJpaRepository;

    @Override
    public FolderEntity save(FolderEntity entity) {
        return folderJpaRepository.save(entity);
    }

    @Override
    public void delete(FolderEntity entity) { folderJpaRepository.delete(entity); }

    @Override
    public Page<FolderEntity> getFolders(UserEntity user, Pageable pageable) {
        // TODO : Folder + FolderShare Join을 통해 ACCEPT Folder 로직 추가
        return Page.empty();
    }

    @Override
    public Optional<FolderEntity> getFolder(Long folderId) {
        return folderJpaRepository.findFirstByFolderId(folderId);
    }

    @Override
    public Optional<FolderEntity> getDefaultFolder(Long userId) {
        return folderJpaRepository.findByDefaultFolder(userId);
    }

    @Override
    public Optional<FolderEntity> getFolderWithoutDefaultFolder(Long folderId, UserEntity user) {
        return folderJpaRepository.findByFolderIdWithoutDefault(folderId, user);
    }
}
