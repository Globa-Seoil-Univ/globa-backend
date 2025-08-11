package org.y2k2.globa.infrastructure.persistence.section.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

@Component
@Primary
public class SectionTestRepositoryImpl extends SectionRepositoryImpl {
    private final SectionJpaRepository sectionRepository;

    public SectionTestRepositoryImpl(SectionJpaRepository sectionJpaRepository) {
        super(sectionJpaRepository);
        this.sectionRepository = sectionJpaRepository;
    }

    public SectionEntity save(SectionEntity entity) {
        return sectionRepository.save(entity);
    }
}
