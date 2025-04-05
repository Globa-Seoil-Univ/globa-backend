package org.y2k2.globa.infrastructure.persistence.highlight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

public interface HighlightJpaRepository extends JpaRepository<HighlightEntity, Long> {
    List<HighlightEntity> findAllBySection_SectionIdIn(List<Long> sectionIds);

    @Query(
            value = "SELECT CASE WHEN EXISTS ( " +
                        "SELECT TRUE FROM highlight h " +
                            "WHERE h.section.sectionId = :sectionId " +
                            "AND (h.startIndex <= :endIndex AND h.endIndex >= :startIndex)" +
                    ") THEN TRUE ELSE FALSE END"
    )
    Boolean existsBySectionAndInRange(Long sectionId, Long startIndex, Long endIndex);

    Optional<HighlightEntity> findByHighlightId(Long highlightId);
}
