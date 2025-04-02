package org.y2k2.globa.factory;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.user.repository.UserRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

@Slf4j
@Getter
@NoArgsConstructor
@Import(UserRepositoryImpl.class)
@Component
public class UserFactory extends CreatorFactory<UserEntity> {
    @Getter(AccessLevel.NONE)
    @Autowired
    private UserRepository userRepository;

    private String code = "ABCDEF";
    private String name = "TESTNAME";
    private String snsId = "1234567890";
    private SnsKind snsKind = SnsKind.GOOGLE;
    private String profilePath = "profilePath";
    private String profileType = "image/jpeg";
    private Long profileSize = 1000L;
    private boolean isDeleted = false;
    private boolean primaryNofi = true;
    private boolean uploadNofi = true;
    private boolean shareNofi = true;
    private boolean eventNofi = true;

    @Builder
    public UserFactory(
            String code,
            String name,
            String snsId,
            SnsKind snsKind,
            String profilePath,
            String profileType,
            Long profileSize,
            boolean isDeleted,
            boolean primaryNofi,
            boolean uploadNofi,
            boolean shareNofi,
            boolean eventNofi
    ) {
        this.code = code;
        this.name = name;
        this.snsId = snsId;
        this.snsKind = snsKind;
        this.profilePath = profilePath;
        this.profileType = profileType;
        this.profileSize = profileSize;
        this.isDeleted = isDeleted;
        this.primaryNofi = primaryNofi;
        this.uploadNofi = uploadNofi;
        this.shareNofi = shareNofi;
        this.eventNofi = eventNofi;
    }

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
            return userRepository.save(entity);
        } else {
            throw new RuntimeException("UserRepository is null, entity will not be persisted");
        }
    }
}
