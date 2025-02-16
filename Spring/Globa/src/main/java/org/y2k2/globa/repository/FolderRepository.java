package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.y2k2.globa.entity.FolderEntity;

import java.util.Optional;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    FolderEntity findFirstByFolderId(Long folderId);
    FolderEntity findFolderEntityByFolderId(Long folderId);
    Optional<FolderEntity> findFirstByUserUserIdOrderByCreatedTimeAsc(Long userId);
}
