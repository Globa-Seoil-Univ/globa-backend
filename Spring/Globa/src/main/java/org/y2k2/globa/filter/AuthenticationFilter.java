package org.y2k2.globa.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.y2k2.globa.dto.common.auth.CustomUserDetails;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.ErrorResponse;
import org.y2k2.globa.service.UserService;
import org.y2k2.globa.util.jwt.JWTProvider;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private final JWTProvider provider;
    private final UserService userService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return path.equals("/user") && method.equals("POST");
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
        }

        filterChain.doFilter(request, response);
    }

    private Authentication getAuthentication(String accessToken) {
        Long userId = provider.getUserIdByAccessToken(accessToken);
        UserEntity user = userService.getUser(userId);
        CustomUserDetails customUser = new CustomUserDetails(user);

        return new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    }
}
