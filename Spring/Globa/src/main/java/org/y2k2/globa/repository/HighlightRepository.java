package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.entity.HighlightEntity;
import org.y2k2.globa.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

public interface HighlightRepository extends JpaRepository<HighlightEntity, Long> {
    List<HighlightEntity> findAllBySectionIn(List<SectionEntity> sections);

    @Query(
            value = "SELECT CASE WHEN EXISTS ( " +
                        "SELECT TRUE FROM highlight h " +
                            "WHERE h.section.sectionId = :sectionId " +
                            "AND (h.startIndex <= :endIndex AND h.endIndex >= :startIndex)" +
                    ") THEN TRUE ELSE FALSE END"
    )
    Boolean existsBySectionAndInRange(Long sectionId, Long startIndex, Long endIndex);

    Optional<HighlightEntity> findByHighlightId(long highlightId);
}
