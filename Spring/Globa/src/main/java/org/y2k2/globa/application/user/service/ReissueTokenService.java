package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.user.command.CreateJWTCommand;
import org.y2k2.globa.application.user.command.VerifyJWTCommand;
import org.y2k2.globa.application.user.usecase.CreateJWTUseCase;
import org.y2k2.globa.application.user.usecase.VerifyJWTUseCase;
import org.y2k2.globa.common.util.jwt.JWT;

@RequiredArgsConstructor
@Service
public class ReissueTokenService {
    private final VerifyJWTUseCase verifyJWTUseCase;
    private final CreateJWTUseCase createJWTUseCase;

    public JWT reissue(String accessToken, String refreshToken) {
        Long userId = verifyJWTUseCase.execute(VerifyJWTCommand.of(accessToken, refreshToken));
        return createJWTUseCase.execute(CreateJWTCommand.of(userId));
    }
}
