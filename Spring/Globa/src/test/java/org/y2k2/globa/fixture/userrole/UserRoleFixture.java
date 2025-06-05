package org.y2k2.globa.fixture.userrole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;
import org.y2k2.globa.infrastructure.persistence.userrole.repository.UserRoleTestRepositoryImpl;

@Import(UserRoleTestRepositoryImpl.class)
@Component
public class UserRoleFixture implements Fixture<UserRoleEntity> {
    @Autowired
    private UserRoleTestRepositoryImpl userRoleRepository;

    @Override
    public UserRoleEntity save(UserRoleEntity entity) {
        return userRoleRepository.save(entity);
    }

    public static UserRoleBuilder builder() {
        return new UserRoleBuilder();
    }

    public static class UserRoleBuilder {
        private UserEntity user;
        private RoleEntity role;

        private UserRoleBuilder() {}

        public UserRoleBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public UserRoleBuilder role(RoleEntity role) {
            this.role = role;
            return this;
        }

        public UserRoleEntity build() {
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setUser(user);
            userRoleEntity.setRole(role);
            return userRoleEntity;
        }
    }
}
