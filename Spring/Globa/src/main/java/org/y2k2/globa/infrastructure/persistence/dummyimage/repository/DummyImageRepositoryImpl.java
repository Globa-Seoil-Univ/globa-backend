package org.y2k2.globa.infrastructure.persistence.dummyimage.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.dummyimage.repository.DummyImageRepository;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class DummyImageRepositoryImpl implements DummyImageRepository {
    private final DummyImageJpaRepository dummyImageJpaRepository;

    @Override
    public DummyImageEntity save(DummyImageEntity entity) {
        return dummyImageJpaRepository.save(entity);
    }

    @Override
    public void deleteAll(List<DummyImageEntity> entities) {
        dummyImageJpaRepository.deleteAllInBatch(entities);
    }

    @Override
    public List<DummyImageEntity> getImages(List<Long> imageIds) {
        return dummyImageJpaRepository.findByImageIdIn(imageIds);
    }
}
