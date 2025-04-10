package org.y2k2.globa.infrastructure.persistence.section.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

public interface SectionJpaRepository extends JpaRepository<SectionEntity, Long> {
    List<SectionEntity> findAllByRecord_RecordIdOrderByStartTimeAsc(Long recordId);
    List<SectionEntity> findAllByRecord(RecordEntity record);

    @Query(
            value = "SELECT s FROM SectionEntity s " +
                    "JOIN FETCH s.record r " +
                    "WHERE s.sectionId = :sectionId " +
                        "AND r.recordId = :recordId " +
                        "AND r.folder.folderId = :folderId "
    )
    Optional<SectionEntity> findBySection(Long sectionId, Long folderId, Long recordId);

    @Query(
            value = "SELECT s, r, f FROM SectionEntity s " +
                    "JOIN FETCH s.record r " +
                    "JOIN FETCH s.record.folder f " +
                    "WHERE s.sectionId = :sectionId " +
                        "AND r.recordId = :recordId " +
                        "AND r.folder.folderId = :folderId "
    )
    Optional<SectionEntity> findByAllJoin(Long sectionId, Long folderId, Long recordId);
}
