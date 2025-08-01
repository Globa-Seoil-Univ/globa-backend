package org.y2k2.globa.infrastructure.persistence.userrole.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

import java.util.List;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, Long> {
    @Modifying
    @Query(
            "DELETE FROM UserRoleEntity ur WHERE ur.user.userId IN (:userIds)"
    )
    void deleteAllByUser_UserIdIn(List<Long> userIds);

    Boolean existsByUser_UserIdAndRole_NameIn(Long userId, List<UserRole> roleName);
}
