package org.y2k2.globa.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.user.command.ValidateJWTCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.common.util.jwt.JWTProvider;
import org.y2k2.globa.common.util.redis.RedisKey;
import org.y2k2.globa.common.util.redis.RedisStore;

@RequiredArgsConstructor
@Component
public class ValidateJWTUseCase implements UseCase<ValidateJWTCommand, Long> {
    private final JWTProvider jwtProvider;
    private final RedisStore redisStore;

    @Override
    public Long execute(ValidateJWTCommand command) {
        Long userId = jwtProvider.getUserIdByAccessTokenWithoutCheck(command.accessToken());
        String refreshToken = redisStore.getValue(RedisKey.REFRESH_KEY.getValue() + userId);

        if (!jwtProvider.isExpired(command.accessToken())) {
            redisStore.deleteValue(userId.toString());
            throw new CustomException(ErrorCode.ACTIVE_ACCESS_TOKEN);
        }

        if (jwtProvider.isExpired(refreshToken)) {
            redisStore.deleteValue(userId.toString());
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        if (!refreshToken.equalsIgnoreCase(command.refreshToken())) {
            redisStore.deleteValue(userId.toString());
            throw new CustomException(ErrorCode.NOT_MATCH_REFRESH_TOKEN);
        }

        return userId;
    }
}
