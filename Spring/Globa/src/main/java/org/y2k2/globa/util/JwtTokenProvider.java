package org.y2k2.globa.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.y2k2.globa.exception.AccessTokenException;
import org.y2k2.globa.exception.GlobalException;
import org.y2k2.globa.exception.RefreshTokenException;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Member 정보를 가지고 AccessToken, RefreshToken을 생성하는 메서드
    public JwtToken generateToken(Long userId) {
        try {
            long now = (new Date()).getTime();

            // Access Token 생성
            Date accessTokenExpiresIn = new Date(now + 86400000); // 86400000는 24시간, 1800000은 30분

            String accessToken = Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .setExpiration(accessTokenExpiresIn)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            // Refresh Token 생성
            String refreshToken = Jwts.builder()
                    .setExpiration(new Date(now + 604800000)) //  604800000는 일주일
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            return JwtToken.builder()
                    .grantType("Bearer")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch ( DnsNameResolverTimeoutException e){
            throw new GlobalException("Redis TimeOut !!!! ");
        }

    }

    public Long getUserIdByAccessTokenWithoutCheck(String token){
        try{
            // Jwt 토큰 복호화
            Claims claims = parseClaims(token);

            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new AccessTokenException("Access Token Expired ! ");
        } catch (SignatureException e ){
            throw new org.y2k2.globa.exception.SignatureException("Not Matched Token");
        }
    }
    public Date getExpiredTimeByAccessTokenWithoutCheck(String token){
        try{
            // Jwt 토큰 복호화
            Claims claims = parseClaims(token);

            return claims.getExpiration();
        } catch (ExpiredJwtException e) {
            throw new AccessTokenException("Access Token Expired ! ");
        } catch (SignatureException e ){
            throw new org.y2k2.globa.exception.SignatureException("Not Matched Token");
        }
    }

    public Long getUserIdByAccessToken(String accessToken) {
        try{
            // Jwt 토큰 복호화
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();


            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new AccessTokenException("Access Token Expired ! ");
        } catch (SignatureException e ){
            throw new org.y2k2.globa.exception.SignatureException("Not Matched Token");
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token) // 지났는지 검사
                    .getBody();

            return claims.getExpiration();
        } catch (ExpiredJwtException e) {
            throw new RefreshTokenException("Refresh Token Expired ! ");
        }
    }


    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
           throw new AccessTokenException("Access Token Expired ! ");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    // accessToken
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }


}
