package org.y2k2.globa.infrastructure.persistence.analysis.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.analysis.repository.AnalysisRepository;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class AnalysisRepositoryImpl implements AnalysisRepository {
    private final AnalysisJpaRepository analysisJpaRepository;

    @Override
    public void deleteAll(List<AnalysisEntity> entities) {
        analysisJpaRepository.deleteAllInBatch(entities);
    }

    @Override
    public List<AnalysisEntity> getAllSections(List<SectionEntity> sections) {
        return analysisJpaRepository.findALlBySectionIn(sections);
    }

    @Override
    public List<AnalysisEntity> getAllSections(SectionEntity section) {
        return analysisJpaRepository.findAllBySection(section);
    }
}
