package com.sentinel.core.service;

import com.sentinel.common.domain.dto.EventDTO;
import com.sentinel.common.domain.entity.Alert;
import com.sentinel.core.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AlertRepository alertRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(alertRepository);
    }

    @Test
    void analyzeEvent_NullSourceIp_DoesNothing() {
        EventDTO event = EventDTO.builder()
                .sourceIp(null)
                .statusCode(200)
                .build();

        analyticsService.analyzeEvent(event);

        verify(alertRepository, never()).save(any());
    }

    @Test
    void analyzeEvent_EmptySourceIp_DoesNothing() {
        EventDTO event = EventDTO.builder()
                .sourceIp("")
                .statusCode(200)
                .build();

        analyticsService.analyzeEvent(event);

        verify(alertRepository, never()).save(any());
    }

    @Test
    void analyzeEvent_NormalEvent_NoAlertGenerated() {
        EventDTO event = EventDTO.builder()
                .sourceIp("192.168.1.1")
                .statusCode(200)
                .severity("INFO")
                .endpoint("/index.html")
                .build();

        analyticsService.analyzeEvent(event);

        verify(alertRepository, never()).save(any());
    }

    @Test
    void analyzeEvent_CriticalSeverity_GeneratesPatternMatchAlert() {
        EventDTO event = EventDTO.builder()
                .sourceIp("10.0.0.1")
                .statusCode(200)
                .severity("CRITICAL")
                .endpoint("/etc/passwd")
                .build();

        analyticsService.analyzeEvent(event);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, atLeastOnce()).save(captor.capture());

        Alert alert = captor.getValue();
        assertEquals("PATTERN_MATCH", alert.getType());
        assertEquals("10.0.0.1", alert.getSourceIp());
        assertTrue(alert.getDescription().contains("Malicious path"));
    }

    @Test
    void analyzeEvent_BruteForce_AfterThresholdExceeded_GeneratesAlert() {
        // Send 11 failed auth events (threshold is 10 per minute)
        for (int i = 0; i < 11; i++) {
            EventDTO event = EventDTO.builder()
                    .sourceIp("192.168.1.100")
                    .statusCode(401)
                    .severity("WARNING")
                    .endpoint("/login")
                    .build();
            analyticsService.analyzeEvent(event);
        }

        // After 11 events, at least one BRUTE_FORCE alert should be generated
        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, atLeastOnce()).save(captor.capture());

        boolean hasBruteForce = captor.getAllValues().stream()
                .anyMatch(a -> "BRUTE_FORCE".equals(a.getType()));
        assertTrue(hasBruteForce, "Expected at least one BRUTE_FORCE alert after threshold exceeded");
    }

    @Test
    void analyzeEvent_Dos_AfterThresholdExceeded_GeneratesAlert() {
        // Send 101 events from the same IP (threshold is 100 per minute)
        for (int i = 0; i < 101; i++) {
            EventDTO event = EventDTO.builder()
                    .sourceIp("10.0.0.50")
                    .statusCode(200)
                    .severity("INFO")
                    .endpoint("/api/data")
                    .build();
            analyticsService.analyzeEvent(event);
        }

        // After 101 events, at least one DOS alert should be generated
        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, atLeastOnce()).save(captor.capture());

        boolean hasDos = captor.getAllValues().stream()
                .anyMatch(a -> "DOS".equals(a.getType()));
        assertTrue(hasDos, "Expected at least one DOS alert after threshold exceeded");
    }
}
