package org.y2k2.globa.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.jwt.JWT;

import java.security.Key;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Slf4j
@Component
public class JWTTestProvider {
    private static final String grantType = "Bearer ";
    private final Key key;

    public JWTTestProvider(@Value("${jwt.secret}") String secretKey) {
        if (secretKey == null) {
            log.info("secretKey가 존재하지 않습니다.");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * UserId를 통해 AccessToken, RefreshToken 생성
     *
     * @param userId 사용자 ID
     * @return {@link JWT}
     */
    public JWT generateToken(Long userId, long accessTokenExpirationTime, long refreshTokenExpirationTime) {
        try {
            CustomTimestamp customTimestamp = new CustomTimestamp();
            LocalDateTime accessTokenExpireTime = customTimestamp.getTimestamp().plusSeconds(accessTokenExpirationTime);
            LocalDateTime refreshTokenExpireTime = customTimestamp.getTimestamp().plusSeconds(refreshTokenExpirationTime);

            String accessToken = Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .setExpiration(Timestamp.valueOf(accessTokenExpireTime))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            String refreshToken = Jwts.builder()
                    .setExpiration(Timestamp.valueOf(refreshTokenExpireTime))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            return JWT.builder()
                    .grantType(grantType)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpireTime(accessTokenExpireTime)
                    .refreshTokenExpireTime(refreshTokenExpireTime)
                    .build();
        } catch (DnsNameResolverTimeoutException e){
            throw new CustomException(ErrorCode.REDIS_TIMEOUT);
        }
    }
}
