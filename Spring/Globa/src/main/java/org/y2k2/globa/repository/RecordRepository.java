package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.entity.RecordEntity;

import java.util.List;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    RecordEntity findRecordEntityByRecordId(Long recordId);
    Page<RecordEntity> findAllByFolderFolderId(Pageable page, Long folderId);

    List<RecordEntity> findRecordEntitiesByUserUserId(Long userId);

    @Query(value = "SELECT * " +
            "FROM record " +
            "WHERE folder_id IN (:folderIds) " +
            "ORDER BY created_time DESC ", nativeQuery = true)
    Page<RecordEntity> findRecordEntitiesByFolder(Pageable pageable, @Param("folderIds") List<Long> folderIds);
  
    RecordEntity findByRecordId(Long recordId);
}
