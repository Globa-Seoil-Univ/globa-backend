package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.y2k2.globa.entity.FolderEntity;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    public FolderEntity findFirstByFolderId(Long folderId);
}
