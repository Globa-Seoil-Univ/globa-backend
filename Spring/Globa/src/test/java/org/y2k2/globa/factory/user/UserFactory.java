package org.y2k2.globa.factory.user;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.user.repository.UserRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

@Slf4j
@Getter
@Setter
@Import(UserRepositoryImpl.class)
@Component
public class UserFactory extends AbstractFactory<UserEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private UserRepository userRepository;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UserEntity lastUser;

    private String code = "ABCDEF";
    private String name = "TESTNAME";
    private String snsId = "1234567890";
    private SnsKind snsKind = SnsKind.GOOGLE;
    private String profilePath = "profilePath";
    private String profileType = "image/jpeg";
    private Long profileSize = 1000L;
    private String fcmToken = "fcmToken";
    private boolean isDeleted = false;
    private boolean primaryNofi = true;
    private boolean uploadNofi = true;
    private boolean shareNofi = true;
    private boolean eventNofi = true;

    @Override
    protected UserEntity create() {
        return new UserEntity();
    }

    @Override
    protected UserEntity setDefaultValues(UserEntity entity) {
        entity.setCode(code);
        entity.setName(name);
        entity.setSnsId(snsId);
        entity.setSnsKind(snsKind);
        entity.setProfilePath(profilePath);
        entity.setProfileType(profileType);
        entity.setProfileSize(profileSize);
        entity.setNotificationToken(fcmToken);
        entity.setNotificationTokenTime(new CustomTimestamp().getTimestamp());
        entity.setIsDeleted(isDeleted);
        entity.setPrimaryNofi(primaryNofi);
        entity.setUploadNofi(uploadNofi);
        entity.setShareNofi(shareNofi);
        entity.setEventNofi(eventNofi);

        return entity;
    }

    @Override
    protected UserEntity saveEntity(UserEntity entity) {
        if (userRepository != null) {
            lastUser = userRepository.save(entity);
            return lastUser;
        } else {
            throw new RuntimeException("UserRepository is null, entity will not be persisted");
        }
    }
}
