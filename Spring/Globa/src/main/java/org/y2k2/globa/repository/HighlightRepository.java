package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.HighlightEntity;
import org.y2k2.globa.entity.SectionEntity;

import java.util.List;

public interface HighlightRepository extends JpaRepository<HighlightEntity, Long> {
    List<HighlightEntity> findAllBySectionSectionId(Long sectionId);
    // sectionId, startIndex, endIndex이 다 같은 거를 가져와라
    HighlightEntity findBySectionAndStartIndexAndEndIndex(SectionEntity section, long startIndex, long endIndex);

    HighlightEntity findByHighlightId(long highlightId);
}
