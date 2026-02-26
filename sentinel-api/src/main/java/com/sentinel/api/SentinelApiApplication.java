package com.sentinel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "com.sentinel.api", "com.sentinel.core" })
@EntityScan(basePackages = "com.sentinel.common.domain.entity")
@EnableJpaRepositories(basePackages = {
        "com.sentinel.core.repository",
        "com.sentinel.api.repository"
})
public class SentinelApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelApiApplication.class, args);
    }
}
