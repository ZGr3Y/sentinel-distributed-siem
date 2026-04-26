package com.sentinel.agent.producer;

import com.sentinel.common.config.RabbitMQConfig;
import com.sentinel.common.domain.dto.EventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private LogProducer logProducer;

    private EventDTO mockEvent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(logProducer, "routingKey", "ingress.routing.key");
        
        mockEvent = new EventDTO();
        mockEvent.setEventId("evt-123");
        mockEvent.setEventType("TEST_EVENT");
        mockEvent.setSourceIp("127.0.0.1");
        mockEvent.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testSendEvent_Success() {
        // Act
        logProducer.sendEvent(mockEvent);

        // Assert
        verify(rabbitTemplate, Mockito.times(1))
                .convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq("ingress.routing.key"), eq(mockEvent));
    }

    @Test
    void testSendEvent_NullEvent_DoesNotSend() {
        // Act
        logProducer.sendEvent(null);

        // Assert
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(EventDTO.class));
    }

    @Test
    void testSendEvent_ExceptionHandling() {
        // Arrange
        Mockito.doThrow(new RuntimeException("RabbitMQ Down"))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(EventDTO.class));

        // Act
        // Shouldn't throw exception externally because it's caught in the method
        logProducer.sendEvent(mockEvent);

        // Assert
        verify(rabbitTemplate, Mockito.times(1))
                .convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq("ingress.routing.key"), eq(mockEvent));
    }
}
