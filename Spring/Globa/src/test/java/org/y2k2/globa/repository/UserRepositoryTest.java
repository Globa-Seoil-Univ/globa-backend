package org.y2k2.globa.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ActiveProfiles;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.helper.UserHelper;

import java.util.List;

@Slf4j
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("유저 Sns ID로 조회 성공")
    void findBySnsId() {
        UserEntity user = UserHelper.createUser();
        userRepository.save(user);

        UserEntity foundUser = userRepository.findBySnsId(user.getSnsId()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getSnsId()).isEqualTo(user.getSnsId());

        log.info("foundUser: {}", foundUser.getName());
    }

    @Test
    @DisplayName("유저 코드로 조회 성공")
    void findOneByCode() {
        UserEntity user = UserHelper.createUser();
        userRepository.save(user);

        UserEntity foundUser = userRepository.findOneByCode(user.getCode()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getCode()).isEqualTo(user.getCode());

        log.info("foundUser: {}", foundUser.getName());
    }

    @Test
    @DisplayName("유저 ID로 조회 성공")
    void findByUserId() {
        UserEntity user = UserHelper.createUser();
        userRepository.save(user);

        UserEntity foundUser = userRepository.findByUserId(user.getUserId()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getUserId()).isEqualTo(user.getUserId());

        log.info("foundUser: {}", foundUser.getName());
    }

    @Test
    @DisplayName("여러 개의 코드로 조회 성공")
    void findAllByCodeIn() {
        UserEntity user = UserHelper.createUser();
        userRepository.save(user);

        List<UserEntity> foundUser = userRepository.findAllByCodeIn(List.of(user.getCode()));

        Assertions.assertThat(foundUser).isNotEmpty();
        Assertions.assertThat(foundUser.size()).isGreaterThan(0);

        log.info("foundUser: {}", foundUser.getFirst().getName());
    }

    @Test
    @DisplayName("유저 저장 성공")
    void saveUser() {
        UserEntity user = UserHelper.createUser();

        UserEntity createdUser = userRepository.save(user);

        Assertions.assertThat(createdUser).isNotNull();
        Assertions.assertThat(createdUser.getUserId()).isNotNull();
        Assertions.assertThat(createdUser.getSnsId()).isEqualTo(user.getSnsId());

        log.info("foundUser: {}", createdUser.getName());
    }

    @Test
    @DisplayName("유저 수정 성공")
    void updateUser() {
        UserEntity user = UserHelper.createUser();
        userRepository.save(user);

        UserEntity foundUser = userRepository.findBySnsId(user.getSnsId()).orElse(null);
        Assertions.assertThat(foundUser).isNotNull();

        foundUser.setName("newNickname");

        UserEntity updatedUser = userRepository.save(foundUser);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getName()).isEqualTo("newNickname");

        log.info("foundUser: {}", foundUser.getName());
    }

    @Test
    @DisplayName("유저 삭제 성공")
    void deleteUser() {
        UserEntity user = UserHelper.createUser();
        userRepository.save(user);

        UserEntity foundUser = userRepository.findBySnsId(user.getSnsId()).orElse(null);
        Assertions.assertThat(foundUser).isNotNull();

        userRepository.delete(foundUser);

        UserEntity deletedUser = userRepository.findBySnsId(user.getSnsId()).orElse(null);
        Assertions.assertThat(deletedUser).isNull();
    }
}
