package org.y2k2.globa.application.user.service;

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
import org.y2k2.globa.application.user.command.VerifyJWTCommand;
import org.y2k2.globa.application.user.service.ReissueTokenService;
import org.y2k2.globa.application.user.usecase.CreateJWTUseCase;
import org.y2k2.globa.application.user.usecase.VerifyJWTUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.jwt.JWT;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ReissueTokenServiceTest {
    @InjectMocks
    private ReissueTokenService reissueTokenService;

    @Mock
    private VerifyJWTUseCase verifyJWTUseCase;
    @Mock
    private CreateJWTUseCase createJWTUseCase;

    private final JWT jwt = JWT.builder()
            .grantType("Bearer")
            .accessToken("access_token")
            .accessTokenExpireTime(new CustomTimestamp().getTimestamp())
            .refreshToken("refresh_token")
            .refreshTokenExpireTime(new CustomTimestamp().getTimestamp())
            .build();

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void reissue() {
        Long userId = 1L;

        Mockito.when(verifyJWTUseCase.execute(VerifyJWTCommand.of("accessToken", "refreshToken")))
                .thenReturn(userId);

        Mockito.when(createJWTUseCase.execute(CreateJWTCommand.of(userId)))
                .thenReturn(jwt);

        JWT result = reissueTokenService.reissue("accessToken", "refreshToken");

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getAccessToken()).isEqualTo("access_token");
        Assertions.assertThat(result.getRefreshToken()).isEqualTo("refresh_token");
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 (AT 만료되지 않음)")
    void reissueInvalidToken() {
        Long userId = 1L;

        Mockito.when(verifyJWTUseCase.execute(VerifyJWTCommand.of("invalidAccessToken", "invalidRefreshToken")))
                .thenThrow(new CustomException(ErrorCode.ACTIVE_ACCESS_TOKEN));

        Assertions.assertThatThrownBy(() -> reissueTokenService.reissue("invalidAccessToken", "invalidRefreshToken"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVE_ACCESS_TOKEN);

        Mockito.verify(createJWTUseCase, Mockito.times(0)).execute(CreateJWTCommand.of(userId));
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 (RT 만료)")
    void reissueRTExpired() {
        Long userId = 1L;

        Mockito.when(verifyJWTUseCase.execute(VerifyJWTCommand.of("accessToken", "expiredRefreshToken")))
                .thenThrow(new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN));

        Assertions.assertThatThrownBy(() -> reissueTokenService.reissue("accessToken", "expiredRefreshToken"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_REFRESH_TOKEN);

        Mockito.verify(createJWTUseCase, Mockito.times(0)).execute(CreateJWTCommand.of(userId));
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 (RT 불일치)")
    void reissueRTNotMatched() {
        Long userId = 1L;

        Mockito.when(verifyJWTUseCase.execute(VerifyJWTCommand.of("accessToken", "mismatchedRefreshToken")))
                .thenThrow(new CustomException(ErrorCode.NOT_MATCH_REFRESH_TOKEN));

        Assertions.assertThatThrownBy(() -> reissueTokenService.reissue("accessToken", "mismatchedRefreshToken"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCH_REFRESH_TOKEN);

        Mockito.verify(createJWTUseCase, Mockito.times(0)).execute(CreateJWTCommand.of(userId));
    }
}
