package org.y2k2.globa.domain.analysis.repository;

import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;

public interface AnalysisRepository {
    void deleteAll(List<AnalysisEntity> entities);

    List<AnalysisEntity> getAllSections(List<SectionEntity> sections);
    List<AnalysisEntity> getAllSections(SectionEntity section);
}
