package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.entity.HighlightEntity;
import org.y2k2.globa.entity.SectionEntity;

import java.util.List;

public interface HighlightRepository extends JpaRepository<HighlightEntity, Long> {
    @Query(value = "SELECT h.highlight_id, h.section_id, h.start_index, h.end_index, h.type, h.created_time " +
                        "FROM highlight h " +
                        "INNER JOIN comment c ON h.highlight_id = c.highlight_id " +
                        "WHERE h.section_id = :sectionId " +
                            "AND (c.parent_id IS NULL OR (c.parent_id IS NOT NULL AND c.deleted = FALSE)) " +
                            "AND c.deleted = FALSE " +
                            "AND NOT EXISTS (" +
                                "SELECT 1 FROM comment child " +
                                    "WHERE child.parent_id = c.comment_id " +
                                    "AND child.deleted = FALSE" +
                            ") " +
                        "GROUP BY h.highlight_id " +
                        "ORDER BY c.created_time DESC",
            nativeQuery = true)
    List<HighlightEntity> findAllBySectionSectionId(Long sectionId);
    // sectionId, startIndex, endIndex이 다 같은 거를 가져와라
    HighlightEntity findBySectionAndStartIndexAndEndIndex(SectionEntity section, long startIndex, long endIndex);

    HighlightEntity findByHighlightId(long highlightId);
}
