package org.y2k2.globa.infrastructure.persistence.dlq.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.dlq.repository.DLQRepository;
import org.y2k2.globa.infrastructure.persistence.dlq.entity.DLQEntity;

@RequiredArgsConstructor
@Repository
public class DLQRepositoryImpl implements DLQRepository {
    private final DLQJpaRepository dlqJpaRepository;

    @Override
    public DLQEntity save(DLQEntity entity) {
        return dlqJpaRepository.save(entity);
    }
}
