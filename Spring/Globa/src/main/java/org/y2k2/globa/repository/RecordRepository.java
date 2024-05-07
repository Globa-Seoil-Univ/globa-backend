package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.RecordEntity;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {
    RecordEntity findByRecordId(Long RecordId);
}
