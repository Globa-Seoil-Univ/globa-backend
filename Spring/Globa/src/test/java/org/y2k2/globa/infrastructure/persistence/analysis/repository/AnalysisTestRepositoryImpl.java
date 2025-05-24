package org.y2k2.globa.infrastructure.persistence.analysis.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;

@Component
@Primary
public class AnalysisTestRepositoryImpl extends AnalysisRepositoryImpl {
    private final AnalysisJpaRepository analysisRepository;

    public AnalysisTestRepositoryImpl(AnalysisJpaRepository analysisRepository) {
        super(analysisRepository);
        this.analysisRepository = analysisRepository;
    }

    public AnalysisEntity save(AnalysisEntity entity) {
        return analysisRepository.save(entity);
    }
}
