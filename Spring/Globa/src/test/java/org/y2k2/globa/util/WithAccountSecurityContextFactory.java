package org.y2k2.globa.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;

public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {
    @Override
    public SecurityContext createSecurityContext(WithAccount annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        CustomUserDetails customUser = new CustomUserDetails(
                1L,
                "TEST_USER_NAME",
                "TEST_FCM_TOKEN"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUser,
                null,
                customUser.getAuthorities()
        );
        context.setAuthentication(auth);
        return context;
    }
}
