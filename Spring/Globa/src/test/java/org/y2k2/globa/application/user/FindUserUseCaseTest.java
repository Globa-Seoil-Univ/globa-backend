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
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
@ExtendWith(SpringExtension.class)
public class FindUserUseCaseTest {
    private FindUserUseCase findUserUseCase;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        findUserUseCase = new FindUserUseCase(userRepository);
    }

    /**
     * 유저 조회 테스트
     * - CacheManager SpringBootTest(통합 테스트)에서만 동작하기 때문에 단위 테스트에서는 확인하지 않음.
     */
    @Test
    @DisplayName("유저 조회 - 성공")
    void getUserTest() {
        Long userId = 1L;
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("isDeleted", false)
                .sample();

        Mockito.when(userRepository.getUserByUserId(userId))
                .thenReturn(Optional.of(user));

        UserEntity result = findUserUseCase.execute(userId);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(user);

        Mockito.verify(userRepository, Mockito.times(1)).getUserByUserId(userId);
    }

    @Test
    @DisplayName("유저 조회 - 실패 (유저가 존재하지 않음)")
    void getUserFailTest() {
        Long userId = 1L;

        Mockito.when(userRepository.getUserByUserId(userId))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> findUserUseCase.execute(userId))
                .as("유저가 존재하지 않는 경우 CustomException이 발생해야 합니다.")
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_USER);

        Mockito.verify(userRepository, Mockito.times(1)).getUserByUserId(userId);
    }

    @Test
    @DisplayName("유저 조회 - 실패 (삭제된 유저)")
    void getUserFailDeletedTest() {
        Long userId = 1L;
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("isDeleted", true)
                .sample();

        Mockito.when(userRepository.getUserByUserId(userId))
                .thenReturn(Optional.of(user));

        Assertions.assertThatThrownBy(() -> findUserUseCase.execute(userId))
                .as("삭제된 유저인 경우 CustomException이 발생해야 합니다.")
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELETED_USER);

        Mockito.verify(userRepository, Mockito.times(1)).getUserByUserId(userId);
    }
}
