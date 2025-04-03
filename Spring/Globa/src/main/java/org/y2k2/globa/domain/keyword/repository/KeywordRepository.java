package org.y2k2.globa.domain.keyword.repository;

import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;

import java.util.List;

public interface KeywordRepository {
    void deleteAll(List<KeywordEntity> entities);

    Boolean hasKeyword(RecordEntity record);

    List<KeywordProjection> getAllByRecordInKeywords(List<RecordEntity> records);
    List<KeywordProjection> getTop10ByAllKeywords(List<Long> recordIds);
    List<KeywordProjection> getTop10ByRecordKeywords(Long recordId);
}
