package org.y2k2.globa.infrastructure.persistence.section.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

public interface SectionJpaRepository extends JpaRepository<SectionEntity, Long> {
    List<SectionEntity> findAllByRecordOrderByStartTimeAsc(RecordEntity record);
    List<SectionEntity> findAllByRecord(RecordEntity record);

    @Query(
            value = "SELECT s FROM SectionEntity s " +
                    "JOIN FETCH s.record r " +
                    "WHERE s.sectionId = :sectionId " +
                        "AND r.recordId = :recordId " +
                        "AND r.folder.folderId = :folderId "
    )
    Optional<SectionEntity> findBySection(Long sectionId, Long folderId, Long recordId);
}
