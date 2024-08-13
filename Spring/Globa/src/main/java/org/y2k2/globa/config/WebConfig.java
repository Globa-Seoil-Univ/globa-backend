package org.y2k2.globa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.y2k2.globa.interceptor.AuthenticationInterceptor;
import org.y2k2.globa.interceptor.PathMatcherInterceptor;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.PathMethod;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${jwt.secret}")
    private String secretKey;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor())
                .order(1)
                .addPathPatterns("/**");
    }

    private HandlerInterceptor authenticationInterceptor() {
        final PathMatcherInterceptor interceptor =
                new PathMatcherInterceptor(new AuthenticationInterceptor(new JwtTokenProvider(secretKey)));

        return interceptor
                .includePathPattern("/user/auth",PathMethod.POST)
                .includePathPattern("/user",PathMethod.GET)
                .includePathPattern("/user/serach",PathMethod.GET)
                .includePathPattern("/user/{_:[0-9]}/notification",PathMethod.GET)
                .includePathPattern("/user/{_:[0-9]}/analysis",PathMethod.GET)
                .includePathPattern("/user/{_:[0-9]}/notification",PathMethod.PUT)
                .includePathPattern("/user/{_:[0-9]}/name",PathMethod.PATCH)
                .includePathPattern("/user",PathMethod.DELETE)
                .includePathPattern("/user/{_:[0-9]}/notification/token",PathMethod.PUT)
                .includePathPattern("/user/{_:[0-9]}/notification/token",PathMethod.POST)
                .includePathPattern("/user/{_:[0-9]}/profile",PathMethod.PATCH)
                .includePathPattern("/folder/{_:[0-9]}/record",PathMethod.GET)
                .includePathPattern("/record",PathMethod.GET)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}",PathMethod.GET)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/analysis",PathMethod.GET)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/quiz",PathMethod.GET)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/quiz",PathMethod.POST)
                .includePathPattern("/folder/{_:[0-9]}/record",PathMethod.POST)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/name",PathMethod.PATCH)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/folder",PathMethod.PATCH)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}",PathMethod.DELETE)
                .includePathPattern("/notification",PathMethod.GET)
                .includePathPattern("/notice/intro",PathMethod.GET)
                .includePathPattern("/notice/{_:[0-9]}",PathMethod.GET)
                .includePathPattern("/notice",PathMethod.POST)
                .includePathPattern("/inquiry",PathMethod.GET)
                .includePathPattern("/inquiry/{_:[0-9]}",PathMethod.GET)
                .includePathPattern("/inquiry",PathMethod.POST)
                .includePathPattern("/folder/{_:[0-9]}/share/user",PathMethod.GET)
                .includePathPattern("/folder/{_:[0-9]}/share/user/{_:[0-9]}",PathMethod.POST)
                .includePathPattern("/folder/{_:[0-9]}/share/user/{_:[0-9]}",PathMethod.PATCH)
                .includePathPattern("/folder/{_:[0-9]}/share/user/{_:[0-9]}",PathMethod.DELETE)
                .includePathPattern("/folder/{_:[0-9]}/share/{_:[0-9]}",PathMethod.POST)
                .includePathPattern("/folder/{_:[0-9]}/share/{_:[0-9]}",PathMethod.DELETE)
                .includePathPattern("/folder",PathMethod.GET)
                .includePathPattern("/folder",PathMethod.POST)
                .includePathPattern("/folder/{_:[0-9]}/name",PathMethod.PATCH)
                .includePathPattern("/folder/{_:[0-9]}",PathMethod.DELETE)
                .includePathPattern("/fcm/send",PathMethod.POST)
                .includePathPattern("/dummy/image",PathMethod.POST)
                .includePathPattern("/dictionary",PathMethod.GET)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/section/{_:[0-9]}/highlight/{_:[0-9]}/comment",PathMethod.GET)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/section/{_:[0-9]}/highlight/{_:[0-9]}/comment/{_:[0-9]}",PathMethod.GET)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/section/{_:[0-9]}",PathMethod.POST)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/section/{_:[0-9]}/highlight/{_:[0-9]}/comment",PathMethod.POST)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/section/{_:[0-9]}/highlight/{_:[0-9]}/comment/{_:[0-9]}",PathMethod.POST)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/section/{_:[0-9]}/highlight/{_:[0-9]}/comment/{_:[0-9]}",PathMethod.PATCH)
                .includePathPattern("/folder/{_:[0-9]}/record/{_:[0-9]}/section/{_:[0-9]}/highlight/{_:[0-9]}/comment/{_:[0-9]}",PathMethod.DELETE)
                .includePathPattern("/inquiry/{_:[0-9]}/answer",PathMethod.POST)
                .includePathPattern("/inquiry/{_:[0-9]}/answer/{_:[0-9]}",PathMethod.PATCH)
                .includePathPattern("/inquiry/{_:[0-9]}/answer",PathMethod.DELETE)

                .excludePathPattern("/user", PathMethod.POST);

    }

}
