package org.y2k2.globa.factory.role;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.role.repository.RoleTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.study.repository.StudyRepositoryImpl;

@Slf4j
@Getter
@Setter
@Import(StudyRepositoryImpl.class)
@Component
public class RoleFactory extends AbstractFactory<RoleEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private RoleTestRepositoryImpl roleRepository;

    private UserRole roleName = UserRole.USER;

    @Override
    protected RoleEntity create() {
        return new RoleEntity();
    }

    @Override
    protected RoleEntity setDefaultValues(RoleEntity entity) {
        entity.setName(roleName);
        return entity;
    }

    @Override
    protected RoleEntity saveEntity(RoleEntity entity) {
        if (roleRepository != null) {
            return roleRepository.save(entity);
        } else {
            throw new RuntimeException("RoleTestRepositoryImpl is null, entity will not be persisted");
        }
    }
}