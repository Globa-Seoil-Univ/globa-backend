package org.y2k2.globa.infrastructure.persistence.role.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.role.repository.RoleJpaRepository;
import org.y2k2.globa.infrastructure.persistence.role.repository.RoleRepositoryImpl;

@Component
@Primary
public class RoleTestRepositoryImpl extends RoleRepositoryImpl {
    private final RoleJpaRepository roleRepository;

    public RoleTestRepositoryImpl(RoleJpaRepository roleJpaRepository, RoleJpaRepository roleRepository) {
        super(roleJpaRepository);
        this.roleRepository = roleRepository;
    }

    public RoleEntity save(RoleEntity entity) {
        return roleRepository.save(entity);
    }
}
