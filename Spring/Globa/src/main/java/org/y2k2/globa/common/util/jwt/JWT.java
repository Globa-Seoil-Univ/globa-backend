package org.y2k2.globa.common.util.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * JWT 토큰 정보를 담는 클래스입니다. <br><br>
 * AccessToken : 인증에 성공한 사용자에게 발급되는 토큰입니다. <br><br>
 * RefreshToken : AccessToken의 만료 시간이 지나면 AccessToken을 갱신하기 위해 사용하는 토큰입니다. <br><br>
 * GrantType : 인증 방식을 나타냅니다. (Bearer)
 * AccessTokenExpireTime : AccessToken의 만료 시간입니다. <br><br>
 * RefreshTokenExpireTime : RefreshToken의 만료 시간입니다.
 */
@Builder
@Getter
@AllArgsConstructor
public class JWT {
    private String grantType;
    private String accessToken;
    private LocalDateTime accessTokenExpireTime;
    private String refreshToken;
    private LocalDateTime refreshTokenExpireTime;
}