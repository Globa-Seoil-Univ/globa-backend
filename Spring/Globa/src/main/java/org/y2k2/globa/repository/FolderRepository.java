package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.y2k2.globa.entity.FolderEntity;

import java.util.List;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    FolderEntity findFirstByFolderId(Long folderId);
    FolderEntity findFolderEntityByFolderId(Long folderId);
    Page<FolderEntity> findAllByUserUserId(Pageable pageable, Long userId);

    Page<FolderEntity> findAllByFolderIdIn(Pageable pageable, List<Long> folderIds);
    FolderEntity findFirstByUserUserIdOrderByCreatedTimeAsc(Long userId);
}
