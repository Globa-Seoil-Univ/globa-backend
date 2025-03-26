package org.y2k2.globa.helper;

import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.jwt.JWT;

public class JWTHelper {
    public static JWT createJWT() {
        return JWT.builder()
                .grantType("Bearer")
                .accessToken("JWT_ACCESS_TOKEN")
                .accessTokenExpireTime(new CustomTimestamp().getTimestamp())
                .refreshToken("JWT_REFRESH_TOKEN")
                .refreshTokenExpireTime(new CustomTimestamp().getTimestamp())
                .build();
    }
}
