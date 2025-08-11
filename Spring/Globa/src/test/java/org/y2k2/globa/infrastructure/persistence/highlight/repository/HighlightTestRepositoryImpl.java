package org.y2k2.globa.infrastructure.persistence.highlight.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;

@Component
@Primary
public class HighlightTestRepositoryImpl extends HighlightRepositoryImpl {
    private final HighlightJpaRepository highlightRepository;

    public HighlightTestRepositoryImpl(HighlightJpaRepository highlightJpaRepository) {
        super(highlightJpaRepository);
        this.highlightRepository = highlightJpaRepository;
    }

    public HighlightEntity save(HighlightEntity entity) {
        return highlightRepository.save(entity);
    }
}
