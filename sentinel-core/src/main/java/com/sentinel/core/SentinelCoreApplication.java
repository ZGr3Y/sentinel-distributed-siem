package com.sentinel.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.sentinel.common.domain.entity")
public class SentinelCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(SentinelCoreApplication.class, args);
    }
}
