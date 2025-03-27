package org.y2k2.globa.infrastructure.persistence.keyword.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;

import java.util.List;

public interface KeywordJpaRepository extends JpaRepository<KeywordEntity, Long> {
    Boolean existsByRecord(RecordEntity record);

    @Query(
            value = "SELECT k.record.recordId AS recordId, k.word AS word, k.importance AS importance " +
                    "FROM KeywordEntity k " +
                    "WHERE k.record IN (:records) " +
                    "ORDER BY k.importance DESC "
    )
    List<KeywordProjection> findAllByRecordInOrderByImportanceDesc(List<RecordEntity> records);

    @Query(value = "SELECT record_id AS recordId, word, AVG(importance) AS importance " +
            "FROM keyword " +
            "WHERE record_id IN (:recordIds) " +
            "GROUP BY word " +
            "ORDER BY COUNT(word) DESC, AVG(importance) DESC " +
            "LIMIT 10;", nativeQuery = true)
    List<KeywordProjection> findKeywordByRecordIds(@Param("recordIds") List<Long> recordIds);

    @Query(
            value = "SELECT k.record.recordId AS recordId, k.word AS word, k.importance AS importance " +
                    "FROM KeywordEntity k " +
                    "WHERE k.record.recordId = :recordId " +
                    "ORDER BY k.importance DESC " +
                    "LIMIT 10"
    )
    List<KeywordProjection> findAllByRecordId(@Param("recordId") Long recordId);
}
