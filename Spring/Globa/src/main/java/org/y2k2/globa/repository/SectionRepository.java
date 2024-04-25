package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.entity.SectionEntity;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {
    @Query(value = """
        SELECT s.section_id, s.record_id, s.title, s.start_time, s.end_time, s.created_time, f.folder_id
        	FROM section s
        		INNER JOIN record r ON s.record_id = r.record_id
        		INNER JOIN folder f ON r.folder_id = f.folder_id
        	WHERE s.section_id = :sectionId
    """, nativeQuery = true)
    SectionEntity findBySectionId(@Param("sectionId") Long sectionId);
}
