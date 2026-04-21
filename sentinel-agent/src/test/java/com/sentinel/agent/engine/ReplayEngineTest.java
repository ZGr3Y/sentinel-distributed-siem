package com.sentinel.agent.engine;

import com.sentinel.agent.parser.NasaLogParser;
import com.sentinel.agent.producer.LogProducer;
import com.sentinel.common.domain.dto.EventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplayEngineTest {

    @Mock
    private NasaLogParser parser;

    @Mock
    private LogProducer producer;

    @Mock
    private Resource logFile;

    @InjectMocks
    private ReplayEngine replayEngine;

    private EventDTO event1;
    private EventDTO event2;

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(replayEngine, "speedupFactor", 1000);
        ReflectionTestUtils.setField(replayEngine, "logFile", logFile);
        
        event1 = new EventDTO();
        event1.setTimestamp(LocalDateTime.of(1995, 7, 1, 0, 0, 1));
        
        event2 = new EventDTO();
        event2.setTimestamp(LocalDateTime.of(1995, 7, 1, 0, 0, 1)); // Same time, delay = 0 to avoid sleeping in test
    }

    @Test
    void testStartReplay_WhenModeIsNotReplay_DoesNothing() {
        // Arrange
        ReflectionTestUtils.setField(replayEngine, "agentMode", "generate");

        // Act
        replayEngine.startReplay();

        // Assert
        verifyNoInteractions(parser);
        verifyNoInteractions(producer);
        verifyNoInteractions(logFile);
    }

    @Test
    void testStartReplay_WhenModeIsReplay_ProcessesLogFile() throws IOException {
        // Arrange
        ReflectionTestUtils.setField(replayEngine, "agentMode", "replay");
        
        String simulatedLogData = "mock line 1\nmock line 2\nmalformed line 3";
        when(logFile.getInputStream()).thenReturn(new ByteArrayInputStream(simulatedLogData.getBytes()));
        
        when(parser.parseLine("mock line 1")).thenReturn(event1);
        when(parser.parseLine("mock line 2")).thenReturn(event2);
        when(parser.parseLine("malformed line 3")).thenReturn(null); // Malformed

        // Act
        replayEngine.startReplay();

        // Assert
        verify(parser, times(3)).parseLine(anyString());
        // producer should only be called twice, since the 3rd line returns null
        verify(producer, times(2)).sendEvent(any(EventDTO.class));
    }
    
    @Test
    void testStartReplay_WhenExceptionThrown_LogsAndExits() throws IOException {
        // Arrange
        ReflectionTestUtils.setField(replayEngine, "agentMode", "replay");
        when(logFile.getInputStream()).thenThrow(new IOException("File not found"));

        // Act
        replayEngine.startReplay();

        // Assert
        verifyNoInteractions(parser);
        verifyNoInteractions(producer);
    }
}
