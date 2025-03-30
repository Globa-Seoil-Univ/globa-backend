package org.y2k2.globa.infrastructure.persistence.userrole.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.userrole.repository.UserRoleRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserRoleRepositoryImpl implements UserRoleRepository {
    private final UserRoleJpaRepository userRoleJpaRepository;

    @Override
    public void save(UserRoleEntity entity) {
        userRoleJpaRepository.save(entity);
    }

    @Override
    public Optional<UserRoleEntity> getUserRole(UserEntity user) {
        return userRoleJpaRepository.findByUser(user);
    }
}
