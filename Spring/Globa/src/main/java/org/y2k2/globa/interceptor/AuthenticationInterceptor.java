package org.y2k2.globa.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.y2k2.globa.util.jwt.JWTProvider;

@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final JWTProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        jwtTokenProvider.getUserIdByAccessToken(request.getHeader("Authorization"));
        return true;
    }
}
