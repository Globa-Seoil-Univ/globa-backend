package org.y2k2.globa.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.y2k2.globa.exception.*;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private static final String grantType = "Bearer";
    private static final long accessTokenExpirationTime = 86400000; // 86400000는 24시간, 1800000은 30분
    private static final long refreshTokenExpirationTime = 604800000; // 일주일
    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
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
     * @return {@link JwtToken}
     */
    public JwtToken generateToken(Long userId) {
        try {
            long now = (new Date()).getTime();

            String accessToken = Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .setExpiration(new Date(now + accessTokenExpirationTime))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            String refreshToken = Jwts.builder()
                    .setExpiration(new Date(now + refreshTokenExpirationTime))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            return JwtToken.builder()
                    .grantType(grantType)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
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
        Claims claims = parseClaims(accessToken, false);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * AccessToken을 통해 만료 시간 반환 (만료 시간 체크 X)
     *
     * @param accessToken AccessToken
     * @return 만료 시간
     */
    public Date getExpiredTimeByAccessTokenWithoutCheck(String accessToken){
        Claims claims = parseClaims(accessToken, false);
        return claims.getExpiration();
    }

    /**
     * RefreshToken을 통해 만료 시간을 검증
     *
     * @param refreshToken RefreshToken
     */
    public void checkExpiredTime(String refreshToken) {
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (SignatureException e){
            throw new CustomException(ErrorCode.SIGNATURE);
        }
    }

    private Claims parseClaims(String accessToken, boolean validate) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        if (accessToken.contains("Bearer")) {
            accessToken = accessToken.split(" ")[1].trim();
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
        }
    }
}
