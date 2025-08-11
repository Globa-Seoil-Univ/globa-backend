package org.y2k2.globa.infrastructure.persistence.dlq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.dlq.entity.DLQEntity;

public interface DLQJpaRepository extends JpaRepository<DLQEntity, Long> {
}
