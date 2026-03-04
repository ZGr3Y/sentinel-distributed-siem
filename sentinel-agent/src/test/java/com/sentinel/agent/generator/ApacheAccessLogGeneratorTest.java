package com.sentinel.agent.generator;

import com.sentinel.common.domain.dto.EventDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApacheAccessLogGeneratorTest {

    private final ApacheAccessLogGenerator generator = new ApacheAccessLogGenerator();

    @Test
    void testGenerateLog() {
        EventDTO event = generator.generateLog();

        assertNotNull(event);
        assertNotNull(event.getTimestamp());
        assertNotNull(event.getSourceIp());
        assertNotNull(event.getMethod());
        assertNotNull(event.getEndpoint());
        assertTrue(event.getStatusCode() >= 100 && event.getStatusCode() <= 599);
        assertTrue(event.getBytes() >= 100 && event.getBytes() <= 50000);
    }
}
