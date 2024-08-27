package org.y2k2.globa.repository;

import org.hibernate.type.descriptor.jdbc.TinyIntAsSmallIntJdbcType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, TinyIntAsSmallIntJdbcType> {
    RoleEntity findByRoleId(long roleId);
}
