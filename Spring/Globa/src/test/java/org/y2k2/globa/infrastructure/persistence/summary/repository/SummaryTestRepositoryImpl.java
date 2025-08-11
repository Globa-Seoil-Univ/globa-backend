package org.y2k2.globa.infrastructure.persistence.summary.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

@Component
@Primary
public class SummaryTestRepositoryImpl extends SummaryRepositoryImpl {
    private final SummaryJpaRepository summaryRepository;

    public SummaryTestRepositoryImpl(SummaryJpaRepository summaryJpaRepository) {
        super(summaryJpaRepository);
        this.summaryRepository = summaryJpaRepository;
    }

    public SummaryEntity save(SummaryEntity entity) {
        return summaryRepository.save(entity);
    }
}
