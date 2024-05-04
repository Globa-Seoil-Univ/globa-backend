package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.dto.Summary;
import org.y2k2.globa.entity.SummaryEntity;

import java.util.List;

public interface SummaryRepository extends JpaRepository<SummaryEntity, Long> {
    List<SummaryEntity> findAllBySectionSectionId(Long sectionId);
}
