package org.y2k2.globa.infrastructure.persistence.userrole.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

@Component
@Primary
public class UserRoleTestRepositoryImpl extends UserRoleRepositoryImpl {
    private final UserRoleJpaRepository userRoleRepository;

    public UserRoleTestRepositoryImpl(UserRoleJpaRepository userRoleJpaRepository) {
        super(userRoleJpaRepository);
        this.userRoleRepository = userRoleJpaRepository;
    }

    public UserRoleEntity save(UserRoleEntity entity) {
        return userRoleRepository.save(entity);
    }
}
