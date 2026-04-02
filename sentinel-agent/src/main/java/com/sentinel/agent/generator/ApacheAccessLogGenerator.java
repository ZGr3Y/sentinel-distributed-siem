package com.sentinel.agent.generator;

import com.sentinel.common.domain.dto.EventDTO;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
public class ApacheAccessLogGenerator implements LogGenerator {

    private final Faker faker;
    private final Queue<EventDTO> attackQueue;
    private final List<String> normalUserIps;

    private static final int POOL_SIZE = 50;

    public ApacheAccessLogGenerator() {
        this.faker = new Faker();
        this.attackQueue = new LinkedList<>();
        this.normalUserIps = new ArrayList<>();
        
        for (int i = 0; i < POOL_SIZE; i++) {
            this.normalUserIps.add(faker.internet().publicIpV4Address());
        }
    }

    @Override
    public EventDTO generateLog() {
        if (!attackQueue.isEmpty()) {
            return attackQueue.poll();
        }

        // 5% chance to trigger an attack sequence when not currently attacking
        if (faker.number().numberBetween(1, 100) <= 5) {
            injectAttackScenario();
            if (!attackQueue.isEmpty()) {
                return attackQueue.poll();
            }
        }

        return generateNormalTraffic();
    }

    private void injectAttackScenario() {
        String attackerIp = faker.internet().publicIpV4Address();
        
        // Tuning: 45% chance DoS, 45% Brute Force, 10% Pattern Match
        int roll = faker.number().numberBetween(1, 100);
        int scenario = (roll <= 45) ? 1 : (roll <= 90) ? 2 : 3;

        switch (scenario) {
            case 1: // DoS Attack (150 reqs to drastically exceed 100 req/min)
                for (int i = 0; i < 150; i++) {
                    attackQueue.add(EventDTO.builder()
                            .timestamp(LocalDateTime.now()) 
                            .sourceIp(attackerIp)
                            .method("GET")
                            .endpoint(generateNormalEndpoint())
                            .statusCode(200)
                            // Use i in bytes to guarantee 100% unique DB hashes, bypassing idempotency filter dropping packets
                            .bytes(500L + i) 
                            .build());
                }
                break;
            case 2: // Brute Force (20 reqs to /login to safely exceed 10 fail/min)
                for (int i = 0; i < 20; i++) {
                    attackQueue.add(EventDTO.builder()
                            .timestamp(LocalDateTime.now())
                            .sourceIp(attackerIp)
                            .method("POST")
                            .endpoint("/login")
                            .statusCode(faker.options().option(401, 403))
                            .bytes(100L + i) // Guarantee unique hash
                            .build());
                }
                break;
            case 3: // Pattern Match / Payload
                String[] maliciousPaths = {
                        "/api/v1/data?file=../../../../etc/passwd",
                        "/cgi-bin/test.sh?cmd=cmd.exe",
                        "/admin/config?exec=/bin/sh"
                };
                attackQueue.add(EventDTO.builder()
                        .timestamp(LocalDateTime.now())
                        .sourceIp(attackerIp)
                        .method("GET")
                        .endpoint(faker.options().option(maliciousPaths))
                        .statusCode(200)
                        .bytes(faker.number().numberBetween(100L, 500L))
                        .build());
                break;
        }
    }

    private EventDTO generateNormalTraffic() {
        String ip = normalUserIps.get(faker.number().numberBetween(0, normalUserIps.size()));
        return EventDTO.builder()
                .timestamp(LocalDateTime.now())
                .sourceIp(ip)
                .method(faker.options().option("GET", "POST", "PUT", "DELETE", "HEAD"))
                .endpoint(generateNormalEndpoint())
                .statusCode(generateNormalStatusCode())
                .bytes(faker.number().numberBetween(100L, 50000L))
                .build();
    }

    private String generateNormalEndpoint() {
        String[] paths = {
                "/index.html", "/login", "/api/v1/users", "/images/logo.png",
                "/about", "/contact", "/api/v1/data", "/admin/dashboard",
                "/.env", "/wp-login.php"
        };
        return faker.options().option(paths);
    }

    private int generateNormalStatusCode() {
        // Skew towards 200 OK
        int roll = faker.number().numberBetween(1, 100);
        if (roll <= 80) return 200; // 80% OK
        if (roll <= 85) return 201; // 5% Created
        if (roll <= 90) return 401; // 5% Unauthorized (normal failures, spread across many IPs so no brute force)
        if (roll <= 95) return 404; // 5% Not Found
        return 500; // 5% Server Error
    }
}
