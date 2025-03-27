package org.y2k2.globa.infrastructure.persistence.highlight.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class HighlightRepositoryImpl implements HighlightRepository {
    private final HighlightJpaRepository highlightJpaRepository;

    @Override
    public HighlightEntity save(HighlightEntity entity) {
        return highlightJpaRepository.save(entity);
    }

    @Override
    public void delete(HighlightEntity entity) {
        highlightJpaRepository.delete(entity);
    }

    @Override
    public Boolean hasHighlightInRange(Long sectionId, Long startIndex, Long endIndex) {
        return highlightJpaRepository.existsBySectionAndInRange(sectionId, startIndex, endIndex);
    }

    @Override
    public List<HighlightEntity> getAllHighlights(List<SectionEntity> sections) {
        return highlightJpaRepository.findAllBySectionIn(sections);
    }

    @Override
    public Optional<HighlightEntity> getHighlight(Long highlightId) {
        return highlightJpaRepository.findByHighlightId(highlightId);
    }
}
