package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.entity.SectionEntity;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {
    @EntityGraph(value = "Section.getSectionAndRecordAndFolder", attributePaths = {
            "record",
            "record.folder"
    }, type = EntityGraph.EntityGraphType.LOAD)
    SectionEntity findBySectionId(@Param("sectionId") long sectionId);
}
