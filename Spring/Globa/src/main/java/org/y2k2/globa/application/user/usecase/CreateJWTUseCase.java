package org.y2k2.globa.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.user.command.CreateJWTCommand;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.common.util.jwt.JWTProvider;
import org.y2k2.globa.common.util.redis.RedisKey;
import org.y2k2.globa.common.util.redis.RedisStore;

@RequiredArgsConstructor
@Component
public class CreateJWTUseCase implements UseCase<CreateJWTCommand, JWT> {
    private final JWTProvider jwtProvider;
    private final RedisStore redisStore;

    @Override
    public JWT execute(CreateJWTCommand command) {
        JWT jwt = jwtProvider.generateToken(command.userId());
        redisStore.setValueExpire(
                RedisKey.REFRESH_KEY.getValue() + command.userId(),
                jwt.getRefreshToken(),
                jwt.getRefreshTokenExpireTime()
        );

        return jwt;
    }
}
