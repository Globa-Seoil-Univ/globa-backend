package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.HighlightEntity;

import java.util.List;

public interface HighlightRepository extends JpaRepository<HighlightEntity, Long> {
    List<HighlightEntity> findAllBySectionSectionId(Long sectionId);
}
