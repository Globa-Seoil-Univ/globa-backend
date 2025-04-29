package org.y2k2.globa.domain.folder.repository;

import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

public interface FolderRepository {
    FolderEntity save(FolderEntity entity);
    void delete(FolderEntity entity);

    Optional<FolderEntity> getFolder(Long folderId);
    Optional<FolderEntity> getDefaultFolder(Long userId);
    Optional<FolderEntity> getFolderWithoutDefaultFolder(Long folderId, UserEntity user);
}
