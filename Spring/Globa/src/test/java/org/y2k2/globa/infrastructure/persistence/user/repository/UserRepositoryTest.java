package org.y2k2.globa.infrastructure.persistence.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.user.repository.UserRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

import java.util.List;

@Slf4j
@Import(UserRepositoryImpl.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {
    private final String TEST_SNS_ID = "testSnsId";
    private final String TEST_NAME = "testName";
    private final String TEST_CODE = "USER01";

    @Autowired
    private UserRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setSnsKind(SnsKind.KAKAO);
        user.setSnsId(TEST_SNS_ID);
        user.setName(TEST_NAME);
        user.setCode(TEST_CODE);
        userRepository.save(user);
    }

    @Test
    @DisplayName("유저 Sns ID로 조회 - 성공")
    void getUserBySnsId() {
        UserEntity foundedUser = userRepository.getUserBySnsId(user.getSnsId())
                .orElse(null);

        Assertions.assertThat(foundedUser).isNotNull();
        Assertions.assertThat(foundedUser.getSnsId()).isEqualTo(TEST_SNS_ID);
        Assertions.assertThat(foundedUser).isEqualTo(user);

        log.info("foundUser snsId = {}", foundedUser.getSnsId());
    }

    @Test
    @DisplayName("유저 코드로 조회 - 성공")
    void getUserByCode() {
        UserEntity foundUser = userRepository.getUserByCode(user.getCode()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getCode()).isEqualTo(TEST_CODE);
        Assertions.assertThat(foundUser).isEqualTo(user);

        log.info("foundUser code = {}", foundUser.getCode());
    }

    @Test
    @DisplayName("유저 ID로 조회 - 성공")
    void getUserById() {
        UserEntity foundUser = userRepository.getUserByUserId(user.getUserId()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getUserId()).isGreaterThan(0);
        Assertions.assertThat(foundUser).isEqualTo(user);

        log.info("foundUser ID = {}", foundUser.getUserId());
    }

    @Test
    @DisplayName("여러 개의 코드로 조회 - 성공")
    void getAllUsersByCodes() {
        List<UserEntity> foundUser = userRepository.getAllUsersByCodes(List.of(user.getCode()));

        Assertions.assertThat(foundUser).isNotEmpty();
        Assertions.assertThat(foundUser.size()).isGreaterThan(0);
        Assertions.assertThat(foundUser.get(0).getCode()).isEqualTo(TEST_CODE);
        Assertions.assertThat(foundUser.get(0)).isEqualTo(user);

        log.info("foundUser = {}", foundUser.get(0).getCode());
    }

    @Test
    @DisplayName("유저 저장 - 성공")
    void saveUser() {
        UserEntity newUser = new UserEntity();
        newUser.setSnsKind(SnsKind.KAKAO);
        newUser.setSnsId("testSnsId2");
        newUser.setName("testName2");
        newUser.setCode("USER02");

        UserEntity createdUser = userRepository.save(newUser);

        Assertions.assertThat(createdUser).isNotNull();
        Assertions.assertThat(createdUser.getUserId()).isNotNull();
        Assertions.assertThat(createdUser.getSnsId()).isEqualTo("testSnsId2");
        Assertions.assertThat(createdUser.getName()).isEqualTo("testName2");
        Assertions.assertThat(createdUser.getCode()).isEqualTo("USER02");

        log.info("newUser = {}, {}, {}", createdUser.getSnsId(), createdUser.getName(), createdUser.getCode());
    }

    @Test
    @DisplayName("유저 이름 수정 - 성공 (이름)")
    void updateUser() {
        user.updateName("newNickname");

        UserEntity updatedUser = userRepository.save(user);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getName()).isEqualTo("newNickname");

        log.info("updateUser = {}", user.getName());
    }

    @Test
    @DisplayName("유저 이름 수정 - 성공 (null)")
    void updateUserNull() {
        // 먼저 이름을 변경
        user.updateName("newNickname");

        // null 값이 들어가면 기존 값이 유지되어야 함
        user.updateName(null);

        UserEntity updatedUser = userRepository.save(user);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getName()).isEqualTo("newNickname");

        log.info("updateUser = {}", user.getName());
    }

    @Test
    @DisplayName("유저 프로필 수정 - 성공")
    void updateUserProfile() {
        FileDto file = FileDto.builder()
                .storeFileName("storeFileName")
                .storePath("storePath")
                .originalFileName("originalFileName")
                .extension("image/jpeg")
                .size(1000L)
                .build();

        user.updateProfile(file);

        UserEntity updatedUser = userRepository.save(user);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getProfilePath()).isEqualTo("storePath");
        Assertions.assertThat(updatedUser.getProfileSize()).isEqualTo(1000L);
        Assertions.assertThat(updatedUser.getProfileType()).isEqualTo("image/jpeg");

        log.info("updateUser = {}, {}, {}", updatedUser.getProfilePath(), updatedUser.getProfileSize(), updatedUser.getProfileType());
    }

    @Test
    @DisplayName("유저 프로필 삭제 - 성공")
    void updateUserProfileDelete() {
        user.updateProfile(null);

        UserEntity updatedUser = userRepository.save(user);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getProfilePath()).isNull();
        Assertions.assertThat(updatedUser.getProfileSize()).isNull();
        Assertions.assertThat(updatedUser.getProfileType()).isNull();
    }

    @Test
    @DisplayName("유저 알림 수정 - 성공")
    void updateUserNotification() {
        user.updateNotification(true, true, true, true);

        UserEntity updatedUser = userRepository.save(user);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getPrimaryNofi()).isTrue();
        Assertions.assertThat(updatedUser.getUploadNofi()).isTrue();
        Assertions.assertThat(updatedUser.getShareNofi()).isTrue();
        Assertions.assertThat(updatedUser.getEventNofi()).isTrue();
    }

    @Test
    @DisplayName("유저 알림 유지 - 성공")
    void updateUserNotificationNull() {
        // 먼저 알림 설정을 true로 변경
        user.updateNotification(true, true, true, true);

        // null 값이 들어가면 기존 값이 유지되어야 함
        user.updateNotification(null,null, null, null);

        UserEntity updatedUser = userRepository.save(user);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getPrimaryNofi()).isTrue();
        Assertions.assertThat(updatedUser.getUploadNofi()).isTrue();
        Assertions.assertThat(updatedUser.getShareNofi()).isTrue();
        Assertions.assertThat(updatedUser.getEventNofi()).isTrue();
    }

    @Test
    @DisplayName("유저 삭제 - 성공 (Soft Delete)")
    void deleteUser() {
        user.delete();

        UserEntity deletedUser = userRepository.save(user);

        Assertions.assertThat(deletedUser).isNotNull();
        Assertions.assertThat(deletedUser.getIsDeleted()).isTrue();
        Assertions.assertThat(deletedUser.getDeletedTime()).isNotNull();
        Assertions.assertThat(deletedUser.getNotificationToken()).isNull();
        Assertions.assertThat(deletedUser.getNotificationTokenTime()).isNull();

        log.info("deletedUser = {}", deletedUser.getDeletedTime());
    }
}
