package org.y2k2.globa.annotation;

import org.springframework.security.test.context.support.WithSecurityContext;
import org.y2k2.globa.util.WithAccountSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAccountSecurityContextFactory.class)
public @interface WithAccount {
    String nickname() default "TESTUSER";
    String fcmToken() default "FCMTOKEN";
}
