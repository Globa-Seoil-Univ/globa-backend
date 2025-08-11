package org.y2k2.globa.common.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.exception.ErrorResponse;
import org.y2k2.globa.common.util.jwt.JWTProvider;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private final JWTProvider provider;

    private final FindUserUseCase findUserUseCase;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return (path.equals("/user") && method.equals("POST"))
                || (path.equals("/user/refresh") && method.equals("POST"))
                || path.endsWith(".html")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        String accessToken = request.getHeader("Authorization");

        if (accessToken == null) {
            response.setStatus(ErrorCode.INVALID_TOKEN.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    ErrorResponse.toJson(ErrorCode.INVALID_TOKEN)
            );
            response.getWriter().flush();
            return;
        }

        try {
            Authentication authentication = getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (CustomException e) {
            response.setStatus(e.getErrorCode().getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    ErrorResponse.toJson(e.getErrorCode())
            );
            response.getWriter().flush();
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Authentication getAuthentication(String accessToken) {
        Long userId = provider.getUserIdByAccessToken(accessToken);
        UserEntity user = findUserUseCase.execute(userId);
        CustomUserDetails customUser = new CustomUserDetails(
                user.getUserId(),
                user.getName(),
                user.getNotificationToken()
        );

        return new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    }
}
