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
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.helper.UserHelper;
import org.y2k2.globa.insfrastructure.persistence.jpa.repository.UserJpaRepository;

import java.util.List;

@Slf4j
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserJpaRepositoryTest {
    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    @DisplayName("유저 Sns ID로 조회 성공")
    void findBySnsId() {
        UserEntity user = UserHelper.createUser();
        userJpaRepository.save(user);

        UserEntity foundUser = userJpaRepository.findBySnsId(user.getSnsId()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getSnsId()).isEqualTo(user.getSnsId());

        log.info("foundUser: {}", foundUser.getName());
    }

    @Test
    @DisplayName("유저 코드로 조회 성공")
    void findOneByCode() {
        UserEntity user = UserHelper.createUser();
        userJpaRepository.save(user);

        UserEntity foundUser = userJpaRepository.findOneByCode(user.getCode()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getCode()).isEqualTo(user.getCode());

        log.info("foundUser: {}", foundUser.getName());
    }

    @Test
    @DisplayName("유저 ID로 조회 성공")
    void findByUserId() {
        UserEntity user = UserHelper.createUser();
        userJpaRepository.save(user);

        UserEntity foundUser = userJpaRepository.findByUserId(user.getUserId()).orElse(null);

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getUserId()).isEqualTo(user.getUserId());

        log.info("foundUser: {}", foundUser.getName());
    }

    @Test
    @DisplayName("여러 개의 코드로 조회 성공")
    void findAllByCodeIn() {
        UserEntity user = UserHelper.createUser();
        userJpaRepository.save(user);

        List<UserEntity> foundUser = userJpaRepository.findAllByCodeIn(List.of(user.getCode()));

        Assertions.assertThat(foundUser).isNotEmpty();
        Assertions.assertThat(foundUser.size()).isGreaterThan(0);

        log.info("foundUser: {}", foundUser.get(0).getName());
    }

    @Test
    @DisplayName("유저 저장 성공")
    void saveUser() {
        UserEntity user = UserHelper.createUser();

        UserEntity createdUser = userJpaRepository.save(user);

        Assertions.assertThat(createdUser).isNotNull();
        Assertions.assertThat(createdUser.getUserId()).isNotNull();
        Assertions.assertThat(createdUser.getSnsId()).isEqualTo(user.getSnsId());

        log.info("foundUser: {}", createdUser.getName());
    }

    @Test
    @DisplayName("유저 수정 성공")
    void updateUser() {
        UserEntity user = UserHelper.createUser();
        userJpaRepository.save(user);

        UserEntity foundUser = userJpaRepository.findBySnsId(user.getSnsId()).orElse(null);
        Assertions.assertThat(foundUser).isNotNull();

        foundUser.setName("newNickname");

        UserEntity updatedUser = userJpaRepository.save(foundUser);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getName()).isEqualTo("newNickname");

        log.info("foundUser: {}", foundUser.getName());
    }

    @Test
    @DisplayName("유저 삭제 성공")
    void deleteUser() {
        UserEntity user = UserHelper.createUser();
        userJpaRepository.save(user);

        UserEntity foundUser = userJpaRepository.findBySnsId(user.getSnsId()).orElse(null);
        Assertions.assertThat(foundUser).isNotNull();

        userJpaRepository.delete(foundUser);

        UserEntity deletedUser = userJpaRepository.findBySnsId(user.getSnsId()).orElse(null);
        Assertions.assertThat(deletedUser).isNull();
    }
}
