package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.KeywordEntity;
import org.y2k2.globa.entity.RecordEntity;

import java.util.List;

public interface KeywordRepository extends JpaRepository<KeywordEntity, Long> {
    List<KeywordEntity> findAllByRecord(RecordEntity record);
}
