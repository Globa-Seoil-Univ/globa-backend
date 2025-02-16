package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.Projection.RecordSearchProjection;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.UserEntity;

import java.util.List;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    RecordEntity findRecordEntityByRecordId(Long recordId);
    Page<RecordEntity> findAllByFolderFolderId(Pageable page, Long folderId);

    @Query(
            value = "SELECT r FROM RecordEntity r " +
                    "JOIN FolderShareEntity fs ON r.folder = fs.folder " +
                    "WHERE fs.ownerUser.userId = :userId OR fs.targetUser.userId = :userId "
    )
    List<RecordEntity> findAllByUser(Long userId);

    @Query(value = "SELECT * " +
            "FROM record " +
            "WHERE folder_id IN (:folderIds) " +
            "ORDER BY created_time DESC ", nativeQuery = true)
    Page<RecordEntity> findRecordEntitiesByFolder(Pageable pageable, @Param("folderIds") List<Long> folderIds);

    List<RecordEntity> findAllByFolder(FolderEntity folder);
  
    RecordEntity findByRecordId(Long recordId);

    @Query(value = "SELECT DISTINCT u.user_id AS userId, u.name AS name, u.profile_path AS profilePath, " +
            "r.record_id AS recordId, f.folder_id AS folderId, r.title AS title, r.created_time AS createdTime " +
            "FROM record r " +
                "JOIN folder f ON r.folder_id = f.folder_id " +
                "JOIN app_user u ON r.user_id = u.user_id " +
                "JOIN folder_share fs ON f.folder_id = fs.folder_id AND fs.invitation_status = 'ACCEPT' " +
            "WHERE (f.user_id = :userId OR fs.target_id = :userId) " +
                "AND r.title LIKE CONCAT('%', :keyword, '%') " +
            "ORDER BY (IF(r.title LIKE CONCAT(:keyword, '%'), 0, 1)), r.created_time DESC",
            nativeQuery = true)
    Page<RecordSearchProjection> findAllSharedOrOwnedRecords(Pageable pageable, @Param("userId") Long userId, @Param("keyword") String keyword);

    @Query(
            value = "SELECT r.record_id, r.folder_id, r.user_id, r.title, r.path, r.size, r.created_time, r.is_share FROM folder_share fs " +
                    "INNER JOIN record r ON fs.folder_id = r.folder_id " +
                    "WHERE (fs.target_id = :userId AND fs.owner_id != :userId) AND fs.invitation_status = 'ACCEPT' " +
                    "ORDER BY r.created_time DESC",
            nativeQuery = true
    )
    Page<RecordEntity> findReceivingRecordsByUserIdOrderByCreatedTimeDesc(Pageable pageable, @Param("userId") Long userId);

    @Query(
            value = "SELECT DISTINCT r.record_id, r.folder_id, r.user_id, r.title, r.path, r.size, r.created_time, r.is_share FROM folder_share fs " +
                    "INNER JOIN record r ON fs.folder_id = r.folder_id " +
                    "WHERE (target_id != :userId AND owner_id = :userId) AND invitation_status = 'ACCEPT' " +
                    "ORDER BY r.created_time DESC",
            nativeQuery = true
    )
    Page<RecordEntity> findSharingRecordsByUserIdOrderByCreatedTimeDesc(Pageable pageable, @Param("userId") Long userId);
}
