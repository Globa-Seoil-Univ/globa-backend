package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.UserEntity;

public interface FolderShareRepository extends JpaRepository<FolderShareEntity, Long> {
    Page<FolderShareEntity> findByFolderOrderByCreatedTimeAsc(Pageable pageable, FolderEntity folder);
    FolderShareEntity findFirstByTargetUser(UserEntity user);
    FolderShareEntity findFirstByFolderAndTargetUser(FolderEntity folderId, UserEntity user);
    FolderShareEntity findFirstByShareId(Long folderId);
}
