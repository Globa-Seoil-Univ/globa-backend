package org.y2k2.globa.fixture.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.user.repository.UserRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

import java.util.UUID;

@Import(UserRepositoryImpl.class)
@Component
public class UserFixture implements Fixture<UserEntity> {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserEntity save(UserEntity entity) {
        return userRepository.save(entity);
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String name = "Default User";
        private final SnsKind snsKind = SnsKind.GOOGLE;
        private Boolean isDeleted = false;

        private UserBuilder() {}

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder isDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public UserEntity build() {
            String code = getRandomString(6);
            String snsId = getRandomString(30);
            String fcmToken = getRandomString(20);

            UserEntity user = new UserEntity();
            user.setName(name);
            user.setCode(code);
            user.setSnsId(snsId);
            user.setSnsKind(snsKind);

            user.setProfilePath("profilePath");
            user.setProfileType("image/jpeg");
            user.setProfileSize(1000L);

            user.setNotificationToken(fcmToken);

            user.setIsDeleted(isDeleted);
            user.setPrimaryNofi(false);
            user.setUploadNofi(false);
            user.setShareNofi(false);
            user.setEventNofi(false);
            return user;
        }

        private String getRandomString(Integer count) {
            if (count == null || count <= 0) {
                count = 10;
            }

            return UUID.randomUUID().toString().substring(0, count);
        }
    }
}
