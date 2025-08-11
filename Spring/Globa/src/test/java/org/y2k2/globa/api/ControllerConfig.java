package org.y2k2.globa.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.y2k2.globa.common.filter.AuthenticationFilter;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.jwt.JWT;

@TestConfiguration
public class ControllerConfig {
    @Bean
    public AuthenticationFilter authenticationFilter() {
        return Mockito.mock(AuthenticationFilter.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean
    public JWT jwt() {
        return JWT.builder()
                .grantType("Bearer ")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .accessTokenExpireTime(new CustomTimestamp().getTimestamp())
                .refreshTokenExpireTime(new CustomTimestamp().getTimestamp())
                .build();
    }
}
