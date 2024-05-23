package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.UserEntity;

import java.util.List;

public interface FolderShareRepository extends JpaRepository<FolderShareEntity, Long> {
    Page<FolderShareEntity> findByFolderOrderByCreatedTimeAsc(Pageable pageable, FolderEntity folder);
    FolderShareEntity findFirstByTargetUserAndFolderFolderId(UserEntity user,Long folderId);
    FolderShareEntity findFirstByShareId(Long folderId);

    List<FolderShareEntity> findFolderShareEntitiesByTargetUser(UserEntity user);
  
    FolderShareEntity findByFolderAndTargetUser(FolderEntity folder, UserEntity user);
    @EntityGraph(value = "FolderShare.getFolderShareAndUser", attributePaths = {
            "targetUser"
    }, type = EntityGraph.EntityGraphType.LOAD)
    List<FolderShareEntity> findAllByFolderFolderId(long folderId);
}
