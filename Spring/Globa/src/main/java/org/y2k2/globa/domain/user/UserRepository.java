package org.y2k2.globa.domain.user;

import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    UserEntity save(UserEntity user);

    Boolean existsByCode(String code);

    Optional<UserEntity> findByUserId(Long userId);
    Optional<UserEntity> findBySnsId(String snsId);
    Optional<UserEntity> findOneByCode(String code);

    List<UserEntity> findAllByCodeIn(List<String> codes);
}
