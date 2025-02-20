package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {
    List<SectionEntity> findAllByRecordOrderByStartTimeAsc(RecordEntity record);

    @Query(
            value = "SELECT s FROM SectionEntity s " +
                    "JOIN FETCH s.record r " +
                    "WHERE s.sectionId = :sectionId " +
                        "AND r.recordId = :recordId " +
                        "AND r.folder.folderId = :folderId "
    )
    Optional<SectionEntity> findBySection(Long sectionId, Long folderId, Long recordId);

    List<SectionEntity> findAllByRecord(RecordEntity record);
}
