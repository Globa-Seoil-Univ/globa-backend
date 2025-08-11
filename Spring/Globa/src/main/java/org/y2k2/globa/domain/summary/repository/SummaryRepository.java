package org.y2k2.globa.domain.summary.repository;

import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.List;

public interface SummaryRepository {
    void deleteAll(List<SummaryEntity> entities);

    List<SummaryEntity> getSummaryInSections(List<Long> sectionIds);
}
