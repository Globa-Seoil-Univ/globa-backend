package org.y2k2.globa.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.annotation.Commit;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.helper.UserHelper;

public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {
    @Override
    @Commit
    public SecurityContext createSecurityContext(WithAccount annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UserEntity user = UserHelper.createUser();
        user.setUserId(1L);

        CustomUserDetails customUser = new CustomUserDetails(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUser,
                null,
                customUser.getAuthorities()
        );
        context.setAuthentication(auth);
        return context;
    }
}
