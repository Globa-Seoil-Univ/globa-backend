package org.y2k2.globa.domain.highlight.repository;

import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

public interface HighlightRepository {
    HighlightEntity save(HighlightEntity entity);
    void delete(HighlightEntity entity);

    Boolean hasHighlightInRange(Long sectionId, Long startIndex, Long endIndex);

    List<HighlightEntity> getAllHighlights(List<SectionEntity> sections);

    Optional<HighlightEntity> getHighlight(Long highlightId);
}
