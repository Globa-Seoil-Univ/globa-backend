package org.y2k2.globa.domain.dlq.repository;

import org.y2k2.globa.infrastructure.persistence.dlq.entity.DLQEntity;

public interface DLQRepository {
    DLQEntity save(DLQEntity entity);
}
