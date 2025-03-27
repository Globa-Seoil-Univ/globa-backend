package org.y2k2.globa.infrastructure.persistence.summary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.List;

public interface SummaryJpaRepository extends JpaRepository<SummaryEntity, Long> {
    List<SummaryEntity> findAllBySectionIn(List<SectionEntity> sections);
}
