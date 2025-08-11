package org.y2k2.globa.infrastructure.persistence.userrole.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.domain.userrole.repository.UserRoleRepository;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserRoleRepositoryImpl implements UserRoleRepository {
    private final UserRoleJpaRepository userRoleJpaRepository;

    @Override
    public UserRoleEntity save(UserRoleEntity entity) {
        userRoleJpaRepository.save(entity);
        return entity;
    }

    @Override
    public void deletes(List<Long> userIds) {
        userRoleJpaRepository.deleteAllByUser_UserIdIn(userIds);
    }

    @Override
    public Boolean isWritable(Long userId) {
        return userRoleJpaRepository.existsByUser_UserIdAndRole_NameIn(
                userId,
                List.of(UserRole.ADMIN, UserRole.EDITOR)
        );
    }
}
