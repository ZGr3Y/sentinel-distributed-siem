package com.sentinel.agent.engine;

import com.sentinel.agent.generator.LogGenerator;
import com.sentinel.agent.producer.LogProducer;
import com.sentinel.common.domain.dto.EventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneratorEngineTest {

    @Mock
    private LogGenerator logGenerator;

    @Mock
    private LogProducer logProducer;

    private GeneratorEngine generatorEngine;

    @BeforeEach
    void setUp() {
        generatorEngine = new GeneratorEngine(logGenerator, logProducer);
        ReflectionTestUtils.setField(generatorEngine, "eventsPerSecond", 100); // Speed up for test
    }

    @Test
    void testStartGeneration_WhenModeIsGenerate() throws InterruptedException {
        ReflectionTestUtils.setField(generatorEngine, "agentMode", "generate");

        EventDTO mockEvent = EventDTO.builder()
                .timestamp(LocalDateTime.now())
                .sourceIp("192.168.1.1")
                .build();

        when(logGenerator.generateLog()).thenReturn(mockEvent);

        generatorEngine.startGeneration();

        // Give the background thread a moment to run
        Thread.sleep(100);

        // Verify that the generator and producer were called
        verify(logGenerator, atLeastOnce()).generateLog();
        verify(logProducer, atLeastOnce()).sendEvent(any(EventDTO.class));
    }

    @Test
    void testStartGeneration_WhenModeIsNotGenerate() throws InterruptedException {
        ReflectionTestUtils.setField(generatorEngine, "agentMode", "replay");

        generatorEngine.startGeneration();

        // Give any potential background thread time to (not) run
        Thread.sleep(50);

        // Verify nothing was generated or produced
        verify(logGenerator, never()).generateLog();
        verify(logProducer, never()).sendEvent(any(EventDTO.class));
    }
}
