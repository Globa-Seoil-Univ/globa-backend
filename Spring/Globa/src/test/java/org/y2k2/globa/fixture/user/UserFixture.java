package org.y2k2.globa.fixture.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.user.UserFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Component
public class UserFixture extends AbstractFixture<UserEntity> {
    @Autowired
    private UserFactory userFactory;

    @Override
    protected UserEntity build() {
        return userFactory.createAndSave();
    }

    public UserFixture withName(String name) {
        userFactory.setName(name);
        return this;
    }

    public UserFixture withCode(String code) {
        userFactory.setCode(code);
        return this;
    }

    public UserFixture withFcmToken(String fcmToken) {
        userFactory.setFcmToken(fcmToken);
        return this;
    }

    public UserFixture withSnsId(String snsId) {
        userFactory.setSnsId(snsId);
        return this;
    }
}
