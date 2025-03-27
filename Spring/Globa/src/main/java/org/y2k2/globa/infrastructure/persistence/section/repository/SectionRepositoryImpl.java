package org.y2k2.globa.infrastructure.persistence.section.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.section.repository.SectionRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class SectionRepositoryImpl implements SectionRepository {
    private final SectionJpaRepository sectionJpaRepository;

    @Override
    public void deleteAll(List<SectionEntity> entities) {
        sectionJpaRepository.deleteAllInBatch(entities);
    }

    @Override
    public List<SectionEntity> getAllSections(RecordEntity record) {
        return sectionJpaRepository.findAllByRecord(record);
    }

    @Override
    public List<SectionEntity> getAllSortedSections(RecordEntity record) {
        return sectionJpaRepository.findAllByRecordOrderByStartTimeAsc(record);
    }

    @Override
    public Optional<SectionEntity> getSection(Long sectionId, Long folderId, Long recordId) {
        return sectionJpaRepository.findBySection(sectionId, folderId, recordId);
    }
}
