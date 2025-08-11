package org.y2k2.globa.infrastructure.persistence.dummyimage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;

import java.util.List;

public interface DummyImageJpaRepository extends JpaRepository<DummyImageEntity, Long> {
    List<DummyImageEntity> findByImageIdIn(List<Long> imageIds);
}
