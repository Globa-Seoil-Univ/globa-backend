package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.RankingEntity;

public interface RankingRepository extends JpaRepository<RankingEntity, Long> {
    Page<RankingEntity> findAll(Pageable pageable);
//    Page<RankingEntity> findRankingEntities (Pageable pageable);

}

