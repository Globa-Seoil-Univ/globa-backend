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
import org.y2k2.globa.application.user.command.CreateJWTCommand;
import org.y2k2.globa.application.user.usecase.CreateJWTUseCase;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.common.util.jwt.JWTProvider;
import org.y2k2.globa.common.util.redis.RedisStore;

@Slf4j
@ExtendWith(SpringExtension.class)
public class CreateJWTUseCaseTest {
    private CreateJWTUseCase createJWTUseCase;

    @MockBean
    private JWTProvider jwtProvider;

    @MockBean
    private RedisStore redisStore;

    @BeforeEach
    void setUp() {
        createJWTUseCase = new CreateJWTUseCase(jwtProvider, redisStore);
    }

    @Test
    @DisplayName("JWT 생성 성공")
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
                        command.userId().toString(),
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
                        command.userId().toString(),
                        jwt.getRefreshToken(),
                        jwt.getRefreshTokenExpireTime()
                );
    }
}
