package org.y2k2.globa.infrastructure.persistence.folder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

public interface FolderJpaRepository extends JpaRepository<FolderEntity, Long> {
    Optional<FolderEntity> findFirstByFolderId(Long folderId);

    @Query(
            "SELECT f FROM FolderEntity f " +
                    "WHERE f.folderId = (" +
                        "SELECT MIN(f2.folderId) FROM FolderEntity f2 " +
                            "WHERE f2.user.userId = :userId " +
                    ")"
    )
    Optional<FolderEntity> findByDefaultFolder(Long userId);

    @Query(
            "SELECT f FROM FolderEntity f " +
                    "WHERE f.folderId = :folderId " +
                    "AND :folderId <> (" +
                        "SELECT MIN(f2.folderId) FROM FolderEntity f2 " +
                            "WHERE f2.user = :user " +
                    ")"
    )
    Optional<FolderEntity> findByFolderIdWithoutDefault(Long folderId, UserEntity user);
}
