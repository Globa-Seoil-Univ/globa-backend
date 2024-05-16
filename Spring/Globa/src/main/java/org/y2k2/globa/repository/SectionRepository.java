package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.SectionEntity;

import java.util.List;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {
    List<SectionEntity> findAllByRecordRecordIdOrderByStartTimeAsc(Long recordId);
    @EntityGraph(value = "Section.getSectionAndRecordAndFolder", attributePaths = {
            "record",
            "record.folder"
    }, type = EntityGraph.EntityGraphType.LOAD)
    SectionEntity findBySectionId(@Param("sectionId") long sectionId);

    SectionEntity findFirstByRecord(RecordEntity record);
}
