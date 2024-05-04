package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.SectionEntity;

import java.util.List;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {

    List<SectionEntity> findAllByRecordRecordId(Long recordId);
}

