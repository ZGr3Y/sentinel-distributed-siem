package com.sentinel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = { "com.sentinel.api", "com.sentinel.core" })
public class SentinelApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelApiApplication.class, args);
    }
}
