package com.sentinel.agent.parser;

import com.sentinel.common.domain.dto.EventDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NasaLogParserTest {

    private final NasaLogParser parser = new NasaLogParser();

    @Test
    void parseLine_ValidLine_ProducesDTO() {
        String input = "199.72.81.55 - - [01/Jul/1995:00:00:01 -0400] \"GET /history/apollo/ HTTP/1.0\" 200 6245";
        EventDTO dto = parser.parseLine(input);

        assertNotNull(dto);
        assertEquals("199.72.81.55", dto.getSourceIp());
        assertEquals("GET", dto.getMethod());
        assertEquals("/history/apollo/", dto.getEndpoint());
        assertEquals(200, dto.getStatusCode());
        assertEquals(6245L, dto.getBytes());
        // Time format check
        assertEquals(1, dto.getTimestamp().getDayOfMonth());
        assertEquals(7, dto.getTimestamp().getMonthValue());
        assertEquals(1995, dto.getTimestamp().getYear());
    }

    @Test
    void parseLine_MinusForBytes_ParsesToZero() {
        String input = "burger.letters.com - - [01/Jul/1995:00:00:11 -0400] \"GET /shuttle/countdown/liftoff.html HTTP/1.0\" 304 -";
        EventDTO dto = parser.parseLine(input);

        assertNotNull(dto);
        assertEquals(0L, dto.getBytes(), "A dash for bytes should be parsed as 0L");
        assertEquals(304, dto.getStatusCode());
    }

    @Test
    void parseLine_MalformedLine_ReturnsNull() {
        String input = "This is a broken line without proper formatting 200";
        EventDTO dto = parser.parseLine(input);

        assertNull(dto, "Malformed lines should return null for safe skipping");
    }

    @Test
    void parseLine_MalformedDate_ReturnsNull() {
        String input = "199.72.81.55 - - [INVALID-DATE] \"GET / HTTP/1.0\" 200 12";
        EventDTO dto = parser.parseLine(input);

        assertNull(dto, "A malformed date should return null (dropped silently)");
    }
}
