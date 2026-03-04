package com.sentinel.agent.generator;

import com.sentinel.common.domain.dto.EventDTO;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ApacheAccessLogGenerator implements LogGenerator {

    private final Faker faker;

    public ApacheAccessLogGenerator() {
        this.faker = new Faker();
    }

    @Override
    public EventDTO generateLog() {
        return EventDTO.builder()
                .timestamp(LocalDateTime.now())
                .sourceIp(faker.internet().publicIpV4Address())
                .method(faker.options().option("GET", "POST", "PUT", "DELETE", "HEAD"))
                .endpoint(generateEndpoint())
                .statusCode(generateStatusCode())
                .bytes(faker.number().numberBetween(100L, 50000L))
                .build();
    }

    private String generateEndpoint() {
        String[] paths = {
                "/index.html", "/login", "/api/v1/users", "/images/logo.png",
                "/about", "/contact", "/api/v1/data", "/admin/dashboard",
                "/.env", "/wp-login.php"
        };
        return faker.options().option(paths);
    }

    private int generateStatusCode() {
        // Skew towards 200 OK
        int roll = faker.number().numberBetween(1, 100);
        if (roll <= 80) return 200; // 80% OK
        if (roll <= 85) return 201; // 5% Created
        if (roll <= 90) return 401; // 5% Unauthorized
        if (roll <= 95) return 404; // 5% Not Found
        return 500; // 5% Server Error
    }
}
