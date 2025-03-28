package org.y2k2.globa.api;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.y2k2.globa.common.filter.AuthenticationFilter;

@TestConfiguration
public class ControllerConfig {
    @Bean
    public AuthenticationFilter authenticationFilter() {
        return Mockito.mock(AuthenticationFilter.class);
    }
}
