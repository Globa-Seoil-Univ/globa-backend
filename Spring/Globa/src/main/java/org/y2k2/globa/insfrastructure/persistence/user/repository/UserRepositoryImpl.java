package org.y2k2.globa.insfrastructure.persistence.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.domain.user.UserRepository;

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
    public Boolean existsByCode(String code) {
        return userJpaRepository.existsByCode(code);
    }

    @Override
    public Optional<UserEntity> findByUserId(Long userId) {
        return userJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<UserEntity> findBySnsId(String snsId) {
        return userJpaRepository.findBySnsId(snsId);
    }

    @Override
    public Optional<UserEntity> findOneByCode(String code) {
        return userJpaRepository.findOneByCode(code);
    }

    @Override
    public List<UserEntity> findAllByCodeIn(List<String> codes) {
        return userJpaRepository.findAllByCodeIn(codes);
    }
}
