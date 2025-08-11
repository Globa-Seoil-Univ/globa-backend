package org.y2k2.globa.infrastructure.persistence.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public UserEntity save(UserEntity user) {
        return userJpaRepository.save(user);
    }

    @Override
    public void deletes(List<UserEntity> users) {
        userJpaRepository.deleteAllInBatch(users);
    }

    @Override
    public Boolean isCodeExists(String code) {
        return userJpaRepository.existsByCode(code);
    }

    @Override
    public List<UserEntity> getAllUsersByCodes(List<String> codes) {
        return userJpaRepository.findAllByCodeIn(codes);
    }

    @Override
    public List<UserEntity> getInActiveUsers() {
        LocalDateTime intervalDay = new CustomTimestamp().getTimestamp().minusDays(30);
        return userJpaRepository.findAllByIsDeletedTrue(intervalDay);
    }

    @Override
    public Optional<UserEntity> getUserBySnsId(String snsId) {
        return userJpaRepository.findBySnsId(snsId);
    }

    @Override
    public Optional<UserEntity> getUserByCode(String code) {
        return userJpaRepository.findByCode(code);
    }

    @Override
    public Optional<UserEntity> getUserByUserId(Long userId) {
        return userJpaRepository.findByUserId(userId);
    }
}
