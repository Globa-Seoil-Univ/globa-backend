package org.y2k2.globa.insfrastructure.persistence.role.repository;

import org.hibernate.type.descriptor.jdbc.TinyIntAsSmallIntJdbcType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.insfrastructure.persistence.role.entity.RoleEntity;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, TinyIntAsSmallIntJdbcType> {
    Optional<RoleEntity> findByName(String roleName);
}
