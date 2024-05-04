package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.RecordEntity;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    RecordEntity findRecordEntityByRecordId(Long recordId);
    Page<RecordEntity> findAllByFolderFolderId(Pageable page, Long folderId);
    Page<RecordEntity> findAllByOrderByCreatedTimeDesc(Pageable page);

}
