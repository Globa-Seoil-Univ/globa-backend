package org.y2k2.globa.infrastructure.persistence.keyword.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class KeywordRepositoryImpl implements KeywordRepository {
    private final KeywordJpaRepository keywordJpaRepository;

    @Override
    public KeywordEntity save(KeywordEntity entity) {
        return keywordJpaRepository.save(entity);
    }

    @Override
    public void deleteAll(List<KeywordEntity> entities) {
        keywordJpaRepository.deleteAllInBatch(entities);
    }

    @Override
    public Boolean hasKeyword(RecordEntity record) {
        return keywordJpaRepository.existsByRecord(record);
    }

    @Override
    public List<KeywordProjection> getAllByRecordInKeywords(List<RecordEntity> records) {
        return keywordJpaRepository.findAllByRecordInOrderByImportanceDesc(records);
    }

    @Override
    public List<KeywordProjection> getTop10ByAllKeywords(List<Long> recordIds) {
        return keywordJpaRepository.findKeywordByRecordIds(recordIds);
    }

    @Override
    public List<KeywordProjection> getTop10ByRecordKeywords(Long recordId) {
        return keywordJpaRepository.findAllByRecordId(recordId);
    }
}
