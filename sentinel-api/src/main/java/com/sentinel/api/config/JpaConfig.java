package com.sentinel.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("!test")
@EntityScan(basePackages = "com.sentinel.common.domain.entity")
@EnableJpaRepositories(basePackages = {
        "com.sentinel.core.repository",
        "com.sentinel.api.repository"
})
public class JpaConfig {
}
