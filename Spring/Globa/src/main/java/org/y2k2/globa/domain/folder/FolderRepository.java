package org.y2k2.globa.domain.folder;

import org.y2k2.globa.insfrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

public interface FolderRepository {
    FolderEntity save(FolderEntity folder);
    Optional<FolderEntity> findByFolderId(Long folderId);
    Optional<FolderEntity> findByDefaultFolder(UserEntity user);
    Optional<FolderEntity> findByFolderIdWithoutDefault(Long folderId, UserEntity user);
    Optional<FolderEntity> findByUserUserIdOrderByCreatedTimeAsc(Long userId);
}
