package org.y2k2.globa.infrastructure.persistence.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {
        "org.y2k2.globa.infrastructure.persistence",
        "org.y2k2.globa.fixture",
        "org.y2k2.globa.factory"
})
public class TestRepositoryConfig {
}
