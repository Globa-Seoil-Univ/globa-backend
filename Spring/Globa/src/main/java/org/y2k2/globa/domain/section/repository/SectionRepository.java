package org.y2k2.globa.domain.section.repository;

import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

public interface SectionRepository {
    void deleteAll(List<SectionEntity> entities);

    List<SectionEntity> getAllSections(RecordEntity record);

    List<SectionEntity> getAllSortedSections(RecordEntity record);

    Optional<SectionEntity> getSection(Long sectionId, Long folderId, Long recordId);
}
