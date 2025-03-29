package org.y2k2.globa.application.user;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.y2k2.globa.application.user.command.ValidateJWTCommand;
import org.y2k2.globa.application.user.usecase.ValidateJWTUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWTProvider;
import org.y2k2.globa.common.util.redis.RedisStore;

@Slf4j
@ExtendWith(SpringExtension.class)
public class ValidateJWTUseCaseTest {
    private ValidateJWTCommand command;
    private ValidateJWTUseCase validateJWTUseCase;

    @MockBean
    private JWTProvider jwtProvider;

    @MockBean
    private RedisStore redisStore;

    @BeforeEach
    void setUp() {
        validateJWTUseCase = new ValidateJWTUseCase(jwtProvider, redisStore);
        command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ValidateJWTCommand.class);
    }

    @Test
    @DisplayName("JWT 검증 성공")
    void validateJWTTest() {
        Long userId = 1L;

        Mockito.when(jwtProvider.getUserIdByAccessTokenWithoutCheck(command.accessToken()))
                .thenReturn(userId);

        Mockito.when(redisStore.getValue(command.accessToken()))
                .thenReturn(command.refreshToken());

        Mockito.when(jwtProvider.isExpired(command.accessToken()))
                .thenReturn(true);

        Mockito.when(jwtProvider.isExpired(command.refreshToken()))
                .thenReturn(false);

        Long response = validateJWTUseCase.execute(command);

        Assertions.assertThat(response)
                .as("검증에 성공하면 AT의 userId를 반환한다.")
                .isNotNull()
                .isEqualTo(userId);

        Mockito.verify(jwtProvider, Mockito.times(1)).getUserIdByAccessTokenWithoutCheck(command.accessToken());
        Mockito.verify(redisStore, Mockito.times(1)).getValue(command.accessToken());
        Mockito.verify(jwtProvider, Mockito.times(1)).isExpired(command.accessToken());
        Mockito.verify(jwtProvider, Mockito.times(1)).isExpired(command.refreshToken());
    }

    @Test
    @DisplayName("JWT 검증 실패 - AT 만료되지 않음")
    void validateJWTATFailTest() {
        Mockito.when(jwtProvider.getUserIdByAccessTokenWithoutCheck(command.accessToken()))
                .thenReturn(1L);

        Mockito.when(redisStore.getValue(command.accessToken()))
                .thenReturn(command.refreshToken());

        Mockito.when(jwtProvider.isExpired(command.accessToken()))
                .thenReturn(false);

        Mockito.when(jwtProvider.isExpired(command.refreshToken()))
                .thenReturn(false);

        Assertions.assertThatThrownBy(() -> validateJWTUseCase.execute(command))
                .as("AT 만료가 되지 않았다면 CustomException을 던진다.")
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVE_ACCESS_TOKEN);

        Mockito.verify(jwtProvider, Mockito.times(1)).getUserIdByAccessTokenWithoutCheck(command.accessToken());
        Mockito.verify(redisStore, Mockito.times(1)).getValue(command.accessToken());
        Mockito.verify(jwtProvider, Mockito.times(1)).isExpired(command.accessToken());
        Mockito.verify(jwtProvider, Mockito.times(0)).isExpired(command.refreshToken());
    }
    
    @Test
    @DisplayName("JWT 검증 실패 - RT 만료됨")
    void validateJWTRTFailTest() {
        Mockito.when(jwtProvider.getUserIdByAccessTokenWithoutCheck(command.accessToken()))
                .thenReturn(1L);

        Mockito.when(redisStore.getValue(command.accessToken()))
                .thenReturn(command.refreshToken());

        Mockito.when(jwtProvider.isExpired(command.accessToken()))
                .thenReturn(true);

        Mockito.when(jwtProvider.isExpired(command.refreshToken()))
                .thenReturn(true);

        Assertions.assertThatThrownBy(() -> validateJWTUseCase.execute(command))
                .as("RT 만료 시 CustomException을 던진다.")
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_REFRESH_TOKEN);

        Mockito.verify(jwtProvider, Mockito.times(1)).getUserIdByAccessTokenWithoutCheck(command.accessToken());
        Mockito.verify(redisStore, Mockito.times(1)).getValue(command.accessToken());
        Mockito.verify(jwtProvider, Mockito.times(1)).isExpired(command.accessToken());
        Mockito.verify(jwtProvider, Mockito.times(1)).isExpired(command.refreshToken());
    }

    @Test
    @DisplayName("JWT 검증 실패 - RT 불일치")
    void validateJWTRTNotMatchFailTest() {
        Mockito.when(jwtProvider.getUserIdByAccessTokenWithoutCheck(command.accessToken()))
                .thenReturn(1L);

        Mockito.when(redisStore.getValue(command.accessToken()))
                .thenReturn("invalid");

        Mockito.when(jwtProvider.isExpired(command.accessToken()))
                .thenReturn(true);

        Mockito.when(jwtProvider.isExpired("invalid"))
                .thenReturn(false);

        Assertions.assertThatThrownBy(() -> validateJWTUseCase.execute(command))
                .as("RT 불일치 시 CustomException을 던진다.")
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCH_REFRESH_TOKEN);

        Mockito.verify(jwtProvider, Mockito.times(1)).getUserIdByAccessTokenWithoutCheck(command.accessToken());
        Mockito.verify(redisStore, Mockito.times(1)).getValue(command.accessToken());
        Mockito.verify(jwtProvider, Mockito.times(1)).isExpired(command.accessToken());
        Mockito.verify(jwtProvider, Mockito.times(1)).isExpired("invalid");
    }
}
