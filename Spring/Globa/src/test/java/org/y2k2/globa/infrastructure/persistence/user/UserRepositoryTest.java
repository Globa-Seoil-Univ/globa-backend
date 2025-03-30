package org.y2k2.globa.infrastructure.persistence.user;

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
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.user.repository.UserJpaRepository;
import org.y2k2.globa.infrastructure.persistence.user.repository.UserRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

@Slf4j
@Import(UserRepositoryImpl.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {
    private final String TEST_SNS_ID = "testSnsId";
    private final String TEST_NAME = "testName";
    private final String TEST_CODE = "ABCABC";

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
    @DisplayName("유저 Sns ID로 조회 성공")
    void getUserBySnsId() {
        UserEntity foundedUser = userRepository.getUserBySnsId(user.getSnsId())
                .orElse(null);

        Assertions.assertThat(foundedUser).isNotNull();
        Assertions.assertThat(foundedUser.getSnsId()).isEqualTo(TEST_SNS_ID);
        Assertions.assertThat(foundedUser).isEqualTo(user);

        log.info("foundUser snsId = {}", foundedUser.getSnsId());
    }

    @Test
    @DisplayName("유저 코드로 조회 성공")
    void getUserByCode() {
        UserEntity foundUser = userRepository.getUserByCode(user.getCode()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getCode()).isEqualTo(TEST_CODE);
        Assertions.assertThat(foundUser).isEqualTo(user);

        log.info("foundUser code = {}", foundUser.getCode());
    }

    @Test
    @DisplayName("유저 ID로 조회 성공")
    void findByUserId() {
        UserEntity foundUser = userRepository.getUserByUserId(user.getUserId()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getUserId()).isGreaterThan(0);
        Assertions.assertThat(foundUser).isEqualTo(user);

        log.info("foundUser ID = {}", foundUser.getUserId());
    }

//    @Test
//    @DisplayName("여러 개의 코드로 조회 성공")
//    void findAllByCodeIn() {
//        UserEntity user = UserHelper.createUser();
//        userJpaRepository.save(user);
//
//        List<UserEntity> foundUser = userJpaRepository.findAllByCodeIn(List.of(user.getCode()));
//
//        Assertions.assertThat(foundUser).isNotEmpty();
//        Assertions.assertThat(foundUser.size()).isGreaterThan(0);
//
//        log.info("foundUser: {}", foundUser.get(0).getName());
//    }
//
//    @Test
//    @DisplayName("유저 저장 성공")
//    void saveUser() {
//        UserEntity user = UserHelper.createUser();
//
//        UserEntity createdUser = userJpaRepository.save(user);
//
//        Assertions.assertThat(createdUser).isNotNull();
//        Assertions.assertThat(createdUser.getUserId()).isNotNull();
//        Assertions.assertThat(createdUser.getSnsId()).isEqualTo(user.getSnsId());
//
//        log.info("foundUser: {}", createdUser.getName());
//    }
//
//    @Test
//    @DisplayName("유저 수정 성공")
//    void updateUser() {
//        UserEntity user = UserHelper.createUser();
//        userJpaRepository.save(user);
//
//        UserEntity foundUser = userJpaRepository.findBySnsId(user.getSnsId()).orElse(null);
//        Assertions.assertThat(foundUser).isNotNull();
//
//        foundUser.setName("newNickname");
//
//        UserEntity updatedUser = userJpaRepository.save(foundUser);
//
//        Assertions.assertThat(updatedUser).isNotNull();
//        Assertions.assertThat(updatedUser.getName()).isEqualTo("newNickname");
//
//        log.info("foundUser: {}", foundUser.getName());
//    }
//
//    @Test
//    @DisplayName("유저 삭제 성공")
//    void deleteUser() {
//        UserEntity user = UserHelper.createUser();
//        userJpaRepository.save(user);
//
//        UserEntity foundUser = userJpaRepository.findBySnsId(user.getSnsId()).orElse(null);
//        Assertions.assertThat(foundUser).isNotNull();
//
//        userJpaRepository.delete(foundUser);
//
//        UserEntity deletedUser = userJpaRepository.findBySnsId(user.getSnsId()).orElse(null);
//        Assertions.assertThat(deletedUser).isNull();
//    }
}
