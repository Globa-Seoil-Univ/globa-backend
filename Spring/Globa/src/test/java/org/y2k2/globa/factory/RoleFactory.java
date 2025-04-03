package org.y2k2.globa.factory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.role.repository.RoleTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.study.repository.StudyRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.study.repository.StudyTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Import(StudyRepositoryImpl.class)
@Component
public class RoleFactory extends CreatorFactory<RoleEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private RoleTestRepositoryImpl roleRepository;

    private String roleName = UserRole.USER.name();

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