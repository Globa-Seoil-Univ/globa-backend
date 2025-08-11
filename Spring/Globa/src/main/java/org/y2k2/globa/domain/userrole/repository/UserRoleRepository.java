package org.y2k2.globa.domain.userrole.repository;

import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository {
    UserRoleEntity save(UserRoleEntity entity);
    void deletes(List<Long> userIds);

    Boolean isWritable(Long userId);
}
