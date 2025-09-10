package org.y2k2.globa.application.user.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.user.command.CreateJWTCommand;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.common.util.jwt.JWTProvider;
import org.y2k2.globa.common.util.redis.RedisKey;
import org.y2k2.globa.common.util.redis.RedisStore;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreateJWTUseCaseTest {
    @InjectMocks
    private CreateJWTUseCase createJWTUseCase;

    @Mock
    private JWTProvider jwtProvider;

    @Mock
    private RedisStore redisStore;

    @Test
    @DisplayName("JWT 생성 - 성공")
    void createJWTTest() {
        CreateJWTCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(CreateJWTCommand.class);

        JWT jwt = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(JWT.class)
                .set("grantType", "Bearer")
                .sample();

        Mockito.when(jwtProvider.generateToken(command.userId()))
                .thenReturn(jwt);

        Mockito.doNothing()
                .when(redisStore)
                .setValueExpire(
                        RedisKey.REFRESH_KEY.getValue() + command.userId().toString(),
                        jwt.getRefreshToken(),
                        jwt.getRefreshTokenExpireTime()
                );

        JWT result = createJWTUseCase.execute(command);

        Assertions.assertThat(result)
                .as("JWT 생성 결과는 null이 아니어야 합니다.")
                .isNotNull()
                .as("JWT 생성 결과는 유효한 JWT여야 합니다.")
                .isEqualTo(jwt)
                .as("JWT의 Grant Type은 Bearer여야 합니다.")
                .hasFieldOrPropertyWithValue("grantType", "Bearer");

        Mockito.verify(jwtProvider, Mockito.times(1)).generateToken(command.userId());
        Mockito.verify(redisStore, Mockito.times(1))
                .setValueExpire(
                        RedisKey.REFRESH_KEY.getValue() + command.userId().toString(),
                        jwt.getRefreshToken(),
                        jwt.getRefreshTokenExpireTime()
                );
    }
}
