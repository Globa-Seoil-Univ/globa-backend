package org.y2k2.globa.infrastructure.persistence.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;

public interface AnalysisJpaRepository extends JpaRepository<AnalysisEntity, Long> {
    List<AnalysisEntity> findALlBySectionIn(List<Long> sectionIds);

    List<AnalysisEntity> findAllBySection(SectionEntity section);
}
