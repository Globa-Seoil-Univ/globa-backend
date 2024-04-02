package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findBySnsId(String snsId);
    UserEntity findOneByUserId(Long userId);
    UserEntity findOneByCode(String code);

    Page<UserEntity> findAll(Pageable pageable);
//    Page<RankingEntity> findRankingEntities (Pageable pageable);

}

