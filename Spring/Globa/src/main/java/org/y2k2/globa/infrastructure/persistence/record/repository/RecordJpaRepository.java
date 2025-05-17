package org.y2k2.globa.infrastructure.persistence.record.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
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
                    "WHERE r.folder.folderId = :folderId"
    )
    List<String> findAllPaths(Long folderId);

    Page<RecordEntity> findAllByFolderFolderId(Long folderId, Pageable pageable);

    @Query(
            value = "SELECT r FROM RecordEntity r " +
                    "JOIN FolderShareEntity fs ON r.folder = fs.folder " +
                    "WHERE " +
                        "fs.targetUser.userId = :userId AND fs.invitationStatus = :status " +
                    "ORDER BY r.recordId DESC"
    )
    Page<RecordEntity> findAllByAccessibleRecord(Long userId, InvitationStatus status, Pageable pageable);

    @Query(
            value = "SELECT DISTINCT u.userId AS userId, u.name AS name, u.profilePath AS profilePath" +
                        ", r.recordId AS recordId, f.folderId AS folderId, r.title AS title, r.createdTime AS createdTime " +
                    "FROM RecordEntity r " +
                    "JOIN UserEntity u ON u = r.user " +
                    "JOIN FolderEntity f ON f = r.folder " +
                    "JOIN FolderShareEntity fs ON fs.folder = f " +
                    "WHERE fs.invitationStatus = :status " +
                        "AND (fs.targetUser.userId = :userId OR fs.ownerUser.userId = :userId) " +
                        "AND r.title LIKE CONCAT('%', :keyword, '%') " +
                    "ORDER BY (CASE WHEN r.title LIKE CONCAT(:keyword, '%') THEN 0 ELSE 1 END)" +
                    ", r.recordId DESC",
            countQuery = "SELECT COUNT(r) " +
                    "FROM RecordEntity r " +
                    "JOIN FolderEntity f ON f = r.folder " +
                    "JOIN UserEntity u ON u = r.user " +
                    "JOIN FolderShareEntity fs ON fs.folder = f " +
                    "WHERE fs.invitationStatus = :status " +
                        "AND (fs.targetUser.userId = :userId OR fs.ownerUser.userId = :userId) " +
                        "AND r.title LIKE CONCAT('%', :keyword, '%') "
    )
    Page<RecordSearchProjection> findAllSharedOrOwnedRecordsByKeyword(Long userId, String keyword, InvitationStatus status, Pageable pageable);

    @Query(
            value = "SELECT r " +
                    "FROM RecordEntity r " +
                    "JOIN FolderShareEntity fs ON r.folder = fs.folder " +
                    "WHERE fs.targetUser.userId = :userId AND fs.ownerUser.userId != :userId " +
                        "AND fs.invitationStatus = :status " +
                    "ORDER BY r.recordId DESC"
    )
    Page<RecordEntity> findReceivingRecordsByUserOrderByCreatedTimeDesc(Long userId, InvitationStatus status, Pageable pageable);

    @Query(
            value = "SELECT DISTINCT r FROM RecordEntity r " +
                    "JOIN FolderShareEntity fs ON r.folder = fs.folder " +
                    "WHERE fs.targetUser.userId != :userId AND fs.ownerUser.userId = :userId " +
                        "AND fs.invitationStatus = :status " +
                    "ORDER BY r.recordId DESC"
    )
    Page<RecordEntity> findSharingRecordsByUserOrderByCreatedTimeDesc(Long userId, InvitationStatus status, Pageable pageable);
}
