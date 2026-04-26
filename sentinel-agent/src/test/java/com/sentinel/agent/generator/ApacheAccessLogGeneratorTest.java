package com.sentinel.agent.generator;

import com.sentinel.common.domain.dto.EventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApacheAccessLogGeneratorTest {

    private ApacheAccessLogGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ApacheAccessLogGenerator();
    }

    @Test
    void testGenerateLog_returnsValidEventDTO() {
        EventDTO event = generator.generateLog();

        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertNotNull(event.getSourceIp());
        assertNotNull(event.getMethod());
        assertNotNull(event.getEndpoint());
        assertNotNull(event.getStatusCode());
        assertNotNull(event.getTimestamp());
        assertTrue(event.getBytes() > 0);
    }

    @Test
    void testGenerateLog_ensuresUniqueUUIDs() {
        Set<String> uuids = new HashSet<>();
        
        // Generate enough logs to trigger both normal and attack scenarios
        for (int i = 0; i < 200; i++) {
            EventDTO event = generator.generateLog();
            assertNotNull(event.getEventId(), "Event ID should never be null");
            assertTrue(uuids.add(event.getEventId()), "Event ID " + event.getEventId() + " was duplicated!");
        }
    }
}
