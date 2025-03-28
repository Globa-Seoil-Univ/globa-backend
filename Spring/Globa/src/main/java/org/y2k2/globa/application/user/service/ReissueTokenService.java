package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.user.command.CreateJWTCommand;
import org.y2k2.globa.application.user.command.ValidateJWTCommand;
import org.y2k2.globa.application.user.usecase.CreateJWTUseCase;
import org.y2k2.globa.application.user.usecase.ValidateJWTUseCase;
import org.y2k2.globa.common.util.jwt.JWT;

@RequiredArgsConstructor
@Service
public class ReissueTokenService {
    private final ValidateJWTUseCase validateJWTUseCase;
    private final CreateJWTUseCase createJWTUseCase;

    public JWT reissue(String accessToken, String refreshToken) {
        Long userId = validateJWTUseCase.execute(ValidateJWTCommand.of(accessToken, refreshToken));
        return createJWTUseCase.execute(CreateJWTCommand.of(userId));
    }
}
