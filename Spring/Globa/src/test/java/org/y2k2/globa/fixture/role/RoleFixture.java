package org.y2k2.globa.fixture.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.factory.role.RoleFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;

@Component
public class RoleFixture extends AbstractFixture<RoleEntity> {
    @Autowired
    private RoleFactory roleFactory;


    @Override
    protected RoleEntity build() {
        return roleFactory.createAndSave();
    }

    public RoleFixture withName(UserRole role) {
        roleFactory.setRoleName(role.name());
        return this;
    }
}
