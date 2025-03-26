package org.y2k2.globa.common.util.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.exception.*;

import java.security.Key;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Component
public class JWTProvider {
    private static final String grantType = "Bearer";
    private static final long accessTokenExpirationTime = 60 * 60 * 24;
    private static final long refreshTokenExpirationTime = 60 * 60 * 24 * 7;
    private final Key key;

    public JWTProvider(@Value("${jwt.secret}") String secretKey) {
        if (secretKey == null) {
            log.info("secretKey가 존재하지 않습니다.");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Member 정보를 가지고 AccessToken, RefreshToken을 생성하는 메서드

    /**
     * UserId를 통해 AccessToken, RefreshToken 생성
     *
     * @param userId 사용자 ID
     * @return {@link JWT}
     */
    public JWT generateToken(Long userId) {
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

    /**
     * AccessToken을 통해 UserId를 반환
     *
     * @param accessToken AccessToken
     * @return UserId
     */
    public Long getUserIdByAccessToken(String accessToken) {
        log.info("getUserIdByAccessToken : {}", accessToken);
        Claims claims = parseClaims(accessToken, true);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * AccessToken을 통해 UserId를 반환 (만료 시간 체크 X)
     *
     * @param accessToken AccessToken
     * @return 만료 시간
     */
    public Long getUserIdByAccessTokenWithoutCheck(String accessToken){
        log.info("getUserIdByAccessTokenWithoutCheck : {}", accessToken);
        Claims claims = parseClaims(accessToken, false);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * token 만료 여부 확인
     *
     * @param token AccessToken
     * @return 만료 여부
     */
    public Boolean isExpired(String token){
        Claims claims = parseClaims(token, false);
        return claims.getExpiration().before(new Date());
    }

    private Claims parseClaims(String accessToken, boolean validate) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (accessToken.contains("Bearer")) {
            accessToken = accessToken.split(" ")[1].trim();
        } else {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            if (validate) {
                throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
            }

            return e.getClaims();
        } catch (SignatureException e) {
            throw new CustomException(ErrorCode.SIGNATURE);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
