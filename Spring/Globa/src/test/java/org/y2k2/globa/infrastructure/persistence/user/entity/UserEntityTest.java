package org.y2k2.globa.infrastructure.persistence.user.entity;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
public class UserEntityTest {
    UserEntity user;

    @BeforeEach
    void setUp() {
        user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(UserEntity.class);
    }

    @Test
    @DisplayName("이름 수정 - 성공")
    void updateName() {
        user.updateName("newName");
        Assertions.assertThat(user.getName()).isEqualTo("newName");
    }

    @Test
    @DisplayName("이름 수정 - 실패 (null)")
    void updateNameFail() {
        user.updateName(null);
        Assertions.assertThat(user.getName()).isNotEqualTo("newName");
    }

    @Test
    @DisplayName("프로필 사진 수정 - 성공")
    void updateProfile() {
        FileDto fileDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(FileDto.class);

        user.updateProfile(fileDto);

        Assertions.assertThat(user.getProfileSize()).isEqualTo(fileDto.size());
        Assertions.assertThat(user.getProfileType()).isEqualTo(fileDto.extension());
        Assertions.assertThat(user.getProfilePath()).isEqualTo(fileDto.storePath());
    }

    @Test
    @DisplayName("프로필 사진 삭제 - 성공")
    void updateProfileFail() {
        user.updateProfile(null);

        Assertions.assertThat(user.getProfileSize()).isNull();
        Assertions.assertThat(user.getProfileType()).isNull();
        Assertions.assertThat(user.getProfilePath()).isNull();
    }

    @Test
    @DisplayName("알림 설정 수정 - 성공")
    void updateNotification() {
        user.updateNotification(true, true, true);

        Assertions.assertThat(user.getUploadNofi()).isTrue();
        Assertions.assertThat(user.getShareNofi()).isTrue();
        Assertions.assertThat(user.getEventNofi()).isTrue();
    }

    @Test
    @DisplayName("알림 설정 수정 - 성공 (null 제외)")
    void updateNotificationExcludeNull() {
        user.updateNotification(true, true, true);
        user.updateNotification(null, false, null);

        Assertions.assertThat(user.getUploadNofi()).isTrue();
        Assertions.assertThat(user.getShareNofi()).isFalse();
        Assertions.assertThat(user.getEventNofi()).isTrue();
    }

    @Test
    @DisplayName("알림 설정 수정 - 실패 (null)")
    void updateNotificationFail() {
        user.updateNotification(true ,true, true);
        user.updateNotification(null, null, null);

        Assertions.assertThat(user.getUploadNofi()).isNotNull();
        Assertions.assertThat(user.getShareNofi()).isNotNull();
        Assertions.assertThat(user.getEventNofi()).isNotNull();
    }

    @Test
    @DisplayName("유저 삭제 - 성공")
    void deleteUser() {
        user.delete();

        Assertions.assertThat(user.getIsDeleted()).isTrue();
        Assertions.assertThat(user.getDeletedTime()).isNotNull();
        Assertions.assertThat(user.getNotificationToken()).isNull();
        Assertions.assertThat(user.getNotificationTokenTime()).isNull();
    }
}
