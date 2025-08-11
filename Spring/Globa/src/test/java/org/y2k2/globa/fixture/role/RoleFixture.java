package org.y2k2.globa.fixture.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.role.repository.RoleTestRepositoryImpl;

@Import(RoleTestRepositoryImpl.class)
@Component
public class RoleFixture implements Fixture<RoleEntity> {
    @Autowired
    private RoleTestRepositoryImpl roleRepository;

    @Override
    public RoleEntity save(RoleEntity entity) {
        return roleRepository.save(entity);
    }

    public RoleEntity getEntity(UserRole role) {
        return roleRepository.getRole(role)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + role));
    }
}
