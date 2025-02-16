package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.UserEntity;

import java.util.Optional;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    Optional<FolderEntity> findFirstByFolderId(Long folderId);
    @Query(
            "SELECT f FROM FolderEntity f " +
                    "WHERE f.folderId = :folderId " +
                    "AND :folderId <> (" +
                        "SELECT MIN(f2.folderId) FROM FolderEntity f2 " +
                            "WHERE f2.user = :user " +
                    ")"
    )
    Optional<FolderEntity> findFirstByFolderIdWithoutDefault(Long folderId, UserEntity user);
    Optional<FolderEntity> findFirstByUserUserIdOrderByCreatedTimeAsc(Long userId);
}
