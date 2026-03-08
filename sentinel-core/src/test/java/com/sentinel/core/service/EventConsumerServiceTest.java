package com.sentinel.core.service;

import com.sentinel.common.domain.dto.EventDTO;
import com.sentinel.core.repository.RawEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventConsumerServiceTest {

    @Mock
    private RawEventRepository repository;

    @Mock
    private AnalyticsService analyticsService;

    private EventConsumerService service;

    // Use a synchronous executor for test predictability
    private final Executor directExecutor = Runnable::run;

    @BeforeEach
    void setUp() {
        service = new EventConsumerService(repository, analyticsService, directExecutor);
    }

    @Test
    void consumeEvent_ValidEvent_SavesAndAnalyzes() {
        EventDTO dto = EventDTO.builder()
                .sourceIp("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .method("GET")
                .endpoint("/index.html")
                .statusCode(200)
                .bytes(1024L)
                .build();

        service.consumeEvent(dto);

        verify(repository).save(any());
        verify(analyticsService).analyzeEvent(any(EventDTO.class));
    }

    @Test
    void consumeEvent_SeverityClassification_CriticalForPathTraversal() {
        EventDTO dto = EventDTO.builder()
                .sourceIp("10.0.0.1")
                .timestamp(LocalDateTime.now())
                .method("GET")
                .endpoint("/../../etc/passwd")
                .statusCode(200)
                .bytes(0L)
                .build();

        service.consumeEvent(dto);

        verify(repository).save(argThat(event -> "CRITICAL".equals(event.getSeverity())));
    }

    @Test
    void consumeEvent_SeverityClassification_WarningFor4xx() {
        EventDTO dto = EventDTO.builder()
                .sourceIp("10.0.0.2")
                .timestamp(LocalDateTime.now())
                .method("GET")
                .endpoint("/missing")
                .statusCode(404)
                .bytes(0L)
                .build();

        service.consumeEvent(dto);

        verify(repository).save(argThat(event -> "WARNING".equals(event.getSeverity())));
    }

    @Test
    void consumeEvent_SeverityClassification_InfoForNormalRequest() {
        EventDTO dto = EventDTO.builder()
                .sourceIp("10.0.0.3")
                .timestamp(LocalDateTime.now())
                .method("GET")
                .endpoint("/index.html")
                .statusCode(200)
                .bytes(1024L)
                .build();

        service.consumeEvent(dto);

        verify(repository).save(argThat(event -> "INFO".equals(event.getSeverity())));
    }
}
