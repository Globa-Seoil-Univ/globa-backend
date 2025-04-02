package org.y2k2.globa.fixture.user;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.UserFactory;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Component
@Getter
public class UserFixture {
    @Autowired
    private UserFactory userFactory;

    public UserEntity createFixture() {
        return createUser();
    }

    private UserEntity createUser() {
        return userFactory.createAndSave();
    }
}
