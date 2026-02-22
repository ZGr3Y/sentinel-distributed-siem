package com.sentinel.common.util;

import com.sentinel.common.domain.dto.EventDTO;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HashUtilsTest {

    @Test
    void testCalculateEventHash_SameObject_ConsistentHash() {
        EventDTO event = EventDTO.builder()
                .sourceIp("192.168.1.10")
                .method("GET")
                .endpoint("/index.html")
                .bytes(4045L)
                .timestamp(LocalDateTime.of(2026, 2, 15, 10, 0))
                .build();

        String hash1 = HashUtils.calculateEventHash(event);
        String hash2 = HashUtils.calculateEventHash(event);

        assertNotNull(hash1);
        assertEquals(hash1, hash2, "Hashing same object twice should produce same result");
        assertEquals(64, hash1.length(), "SHA-256 hex string should be 64 chars long");
    }

    @Test
    void testCalculateEventHash_DifferentObjects_DifferentHash() {
        EventDTO event1 = EventDTO.builder()
                .sourceIp("192.168.1.10")
                .method("GET")
                .endpoint("/index.html")
                .bytes(4045L)
                .timestamp(LocalDateTime.of(2026, 2, 15, 10, 0))
                .build();

        EventDTO event2 = EventDTO.builder()
                .sourceIp("192.168.1.10")
                .method("POST") // Changed method
                .endpoint("/index.html")
                .bytes(4045L)
                .timestamp(LocalDateTime.of(2026, 2, 15, 10, 0))
                .build();

        String hash1 = HashUtils.calculateEventHash(event1);
        String hash2 = HashUtils.calculateEventHash(event2);

        assertNotEquals(hash1, hash2, "Different events should produce different hashes");
    }
}
