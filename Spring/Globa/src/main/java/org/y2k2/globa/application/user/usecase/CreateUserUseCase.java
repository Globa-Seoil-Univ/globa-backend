package org.y2k2.globa.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.user.command.CreateUserCommand;
import org.y2k2.globa.application.user.command.SaveUserCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.common.util.jwt.JWTProvider;
import org.y2k2.globa.common.util.redis.RedisStore;

@RequiredArgsConstructor
@Component
public class CreateUserUseCase implements UseCase<SaveUserCommand, JWT> {
    private final JWTProvider jwtProvider;
    private final RedisStore redisStore;

    @Override
    public JWT execute(SaveUserCommand command) {
        if (command.user().getIsDeleted()) {
            throw new CustomException(ErrorCode.DELETED_USER);
        }

        JWT jwt = jwtProvider.generateToken(command.user().getUserId());
        redisStore.setValueExpire(
                command.user().getUserId().toString(),
                jwt.getRefreshToken(),
                jwt.getRefreshTokenExpireTime()
        );

        return jwt;
    }
}
