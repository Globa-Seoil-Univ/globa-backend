package org.y2k2.globa.application.user;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.y2k2.globa.application.user.usecase.FindActiveUserIdUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
@ExtendWith(SpringExtension.class)
public class FindActiveUserIdUseCaseTest {
    private FindActiveUserIdUseCase findActiveUserIdUseCase;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        findActiveUserIdUseCase = new FindActiveUserIdUseCase(userRepository);
    }

    @Test
    @DisplayName("사용자 고유 ID 조회 성공")
    void getUserIdTest() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("isDeleted", false)
                .sample();

        Mockito.when(userRepository.getUserBySnsId(user.getSnsId()))
                .thenReturn(Optional.of(user));

        Optional<Long> userId = findActiveUserIdUseCase.execute(user.getSnsId());

        Assertions.assertThat(userId)
                .as("존재하는 사용자의 경우 userId를 포함해야 합니다.")
                .isPresent()
                .as("사용자 고유 ID는 null이 아니어야 합니다.")
                .isNotNull();

        Mockito.verify(userRepository, Mockito.times(1)).getUserBySnsId(user.getSnsId());
    }

    @Test
    @DisplayName("사용자 고유 ID 없음")
    void getUserIdNotExistTest() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("isDeleted", false)
                .sample();

        Mockito.when(userRepository.getUserBySnsId(user.getSnsId()))
                .thenReturn(Optional.empty());

        Optional<Long> userId = findActiveUserIdUseCase.execute(user.getSnsId());

        Assertions.assertThat(userId)
                .as("존재하지 않는 사용자의 경우 Optional.empty()를 반환해야 합니다.")
                .isNotPresent();

        Mockito.verify(userRepository, Mockito.times(1)).getUserBySnsId(user.getSnsId());
    }

    @Test
    @DisplayName("사용자 고유 ID 조회 실패 - 삭제된 사용자")
    void getUserIdFailTest() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("isDeleted", true)
                .sample();

        Mockito.when(userRepository.getUserBySnsId(user.getSnsId()))
                .thenReturn(Optional.of(user));

        Assertions.assertThatThrownBy(() -> findActiveUserIdUseCase.execute(user.getSnsId()))
                .as("삭제된 사용자의 경우 CustomException이 발생해야 합니다.")
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELETED_USER);

        Mockito.verify(userRepository, Mockito.times(1)).getUserBySnsId(user.getSnsId());
    }
}
