package org.y2k2.globa.domain.user.repository;

import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    UserEntity save(UserEntity user);
    void deletes(List<UserEntity> users);

    Boolean isCodeExists(String code);

    List<UserEntity> getAllUsersByCodes(List<String> codes);
    List<UserEntity> getInActiveUsers();

    Optional<UserEntity> getUserBySnsId(String snsId);
    Optional<UserEntity> getUserByCode(String code);
    Optional<UserEntity> getUserByUserId(Long userId);
}
