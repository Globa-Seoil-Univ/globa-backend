package org.y2k2.globa.domain.dummyimage.repository;

import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;

import java.util.List;

public interface DummyImageRepository {
    DummyImageEntity save(DummyImageEntity entity);
    void deleteAll(List<DummyImageEntity> entities);

    List<DummyImageEntity> getImages(List<Long> imageIds);
}
