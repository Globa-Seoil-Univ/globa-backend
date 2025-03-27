package org.y2k2.globa.infrastructure.persistence.record.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.record.projection.RecordSearchProjection;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface RecordJpaRepository extends JpaRepository<RecordEntity, Long> {
    @Query(
            value = "SELECT r.recordId FROM RecordEntity r " +
                    "JOIN FolderShareEntity fs ON r.folder = fs.folder " +
                    "WHERE fs.ownerUser.userId = :userId OR fs.targetUser.userId = :userId "
    )
    List<Long> findAllRecordId(Long userId);

    @Query(
            value = "SELECT r.path FROM RecordEntity r " +
                    "JOIN FolderShareEntity fs ON r.folder = fs.folder " +
                    "WHERE fs.ownerUser.userId = :userId OR fs.targetUser.userId = :userId "
    )
    List<String> findAllPaths(FolderEntity folder);

    Page<RecordEntity> findAllByFolderFolderId(Long folderId, Pageable pageable);

    @Query(
            value = "SELECT r FROM RecordEntity r " +
                    "JOIN FolderShareEntity fs ON r.folder = fs.folder " +
                    "WHERE " +
                        "fs.targetUser = :user AND fs.invitationStatus = 'ACCEPT' " +
                    "ORDER BY r.createdTime"
    )
    Page<RecordEntity> findAllByAccessibleRecord(UserEntity user, Pageable pageable);

    @Query(
            value = "SELECT DISTINCT u.userId AS userId, u.name AS name, u.profilePath AS profilePath" +
                    ", r.recordId AS recordId, f.folderId AS folderId, r.title AS title, r.createdTime AS createdTime " +
                    "FROM RecordEntity r " +
                    "JOIN UserEntity u ON u = r.user " +
                    "JOIN FolderEntity f ON f = r.folder " +
                    "JOIN FolderShareEntity fs ON fs.folder = f " +
                    "WHERE (f.user = :user OR fs.targetUser = :user) " +
                    "AND fs.invitationStatus = 'ACCEPT' " +
                    "AND r.title LIKE CONCAT('%', :keyword, '%') " +
                    "ORDER BY (CASE WHEN r.title LIKE CONCAT(:keyword, '%') THEN 0 ELSE 1 END)" +
                    ", r.createdTime DESC",
            countQuery = "SELECT COUNT(r) " +
                    "FROM RecordEntity r " +
                    "JOIN FolderEntity f " +
                    "JOIN UserEntity u " +
                    "JOIN FolderShareEntity fs " +
                    "WHERE (f.user = :user OR fs.targetUser = :user) " +
                    "AND fs.invitationStatus = 'ACCEPT' " +
                    "AND r.title LIKE CONCAT('%', :keyword, '%')"
    )
    Page<RecordSearchProjection> findAllSharedOrOwnedRecordsByKeyword(UserEntity user, String keyword, Pageable pageable);

    @Query(
            value = "SELECT r " +
                    "FROM RecordEntity r " +
                    "JOIN FolderShareEntity fs ON r.folder = fs.folder " +
                    "WHERE fs.targetUser = :user AND fs.ownerUser != :user " +
                    "AND fs.invitationStatus = 'ACCEPT' " +
                    "ORDER BY r.createdTime DESC"
    )
    Page<RecordEntity> findReceivingRecordsByUserOrderByCreatedTimeDesc(UserEntity user, Pageable pageable);

    @Query(
            value = "SELECT DISTINCT r FROM RecordEntity r " +
                    "JOIN FolderShareEntity fs ON r.folder = fs.folder " +
                    "WHERE fs.targetUser != :user AND fs.ownerUser = :user " +
                    "AND fs.invitationStatus = 'ACCEPT' " +
                    "ORDER BY r.createdTime DESC"
    )
    Page<RecordEntity> findSharingRecordsByUserOrderByCreatedTimeDesc(UserEntity user, Pageable pageable);
  
    Optional<RecordEntity> findByRecordId(Long recordId);
}
