package org.y2k2.globa.infrastructure.persistence.summary.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.summary.repository.SummaryRepository;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class SummaryRepositoryImpl implements SummaryRepository {
    private final SummaryJpaRepository summaryJpaRepository;

    @Override
    public void deleteAll(List<SummaryEntity> entities) {
        summaryJpaRepository.deleteAllInBatch(entities);
    }

    @Override
    public List<SummaryEntity> getSummaryInSections(List<Long> sectionIds) {
        return summaryJpaRepository.findAllBySection_SectionIdIn(sectionIds);
    }
}
