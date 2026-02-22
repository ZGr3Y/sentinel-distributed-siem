package com.sentinel.core.service;

import com.sentinel.common.domain.dto.EventDTO;
import com.sentinel.common.domain.entity.Alert;
import com.sentinel.core.repository.AlertRepository;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AlertRepository alertRepository;
    private final RateLimiterRegistry rateLimiterRegistry;

    // Track state to avoid spamming the same alert for the same IP continuously
    private final ConcurrentMap<String, Long> lastDosAlertTime = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> lastBruteForceAlertTime = new ConcurrentHashMap<>();

    private static final long ALERT_COOLDOWN_MS = 60000; // 1 minute cooldown per IP per alert type

    public AnalyticsService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;

        // Configure a RateLimiter: limit to 100 requests per 60 seconds
        RateLimiterConfig dosConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .limitForPeriod(100)
                .timeoutDuration(Duration.ZERO) // Fail immediately if limit is exceeded
                .build();

        // Configure a RateLimiter for Brute Force: limit to 10 failures per 60 seconds
        RateLimiterConfig bruteForceConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .limitForPeriod(10)
                .timeoutDuration(Duration.ZERO)
                .build();

        this.rateLimiterRegistry = RateLimiterRegistry.of(dosConfig);
        this.rateLimiterRegistry.addConfiguration("brute-force", bruteForceConfig);
    }

    public void analyzeEvent(EventDTO event) {
        String sourceIp = event.getSourceIp();
        if (sourceIp == null || sourceIp.isEmpty()) {
            return;
        }

        checkDos(sourceIp);
        checkBruteForce(event);
    }

    private void checkDos(String sourceIp) {
        RateLimiter dosLimiter = rateLimiterRegistry.rateLimiter("dos-" + sourceIp);

        // tryAcquire() returns false if the limit (100 req/min) has been reached
        if (!dosLimiter.acquirePermission()) {
            long now = System.currentTimeMillis();
            long lastAlert = lastDosAlertTime.getOrDefault(sourceIp, 0L);

            // Only generate an alert if we haven't alerted for this IP in the last minute
            if (now - lastAlert > ALERT_COOLDOWN_MS) {
                log.warn("🚨 DOS ATTACK DETECTED from IP: {}", sourceIp);
                createAlert("DOS", sourceIp, "Volume exceeded 100 requests per minute.");
                lastDosAlertTime.put(sourceIp, now);
            }
        }
    }

    private void checkBruteForce(EventDTO event) {
        // Assume failure if status code is 401 or 403
        if (event.getStatusCode() == 401 || event.getStatusCode() == 403) {
            String sourceIp = event.getSourceIp();
            RateLimiter bruteForceLimiter = rateLimiterRegistry.rateLimiter("bf-" + sourceIp, "brute-force");

            // tryAcquire() returns false if the limit (10 failures/min) has been reached
            if (!bruteForceLimiter.acquirePermission()) {
                long now = System.currentTimeMillis();
                long lastAlert = lastBruteForceAlertTime.getOrDefault(sourceIp, 0L);

                if (now - lastAlert > ALERT_COOLDOWN_MS) {
                    log.warn("🚨 BRUTE FORCE ATTACK DETECTED from IP: {}", sourceIp);
                    createAlert("BRUTE_FORCE", sourceIp, "Failed authentication > 10 times in 1 minute.");
                    lastBruteForceAlertTime.put(sourceIp, now);
                }
            }
        }
    }

    private void createAlert(String type, String sourceIp, String description) {
        Alert alert = new Alert(type, sourceIp, description);
        try {
            alertRepository.save(alert);
            log.info("Alert persisted to database: [{}] {}", type, sourceIp);
        } catch (Exception e) {
            log.error("Failed to save alert to database", e);
        }
    }
}
