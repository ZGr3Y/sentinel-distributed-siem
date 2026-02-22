package com.sentinel.agent.parser;

import com.sentinel.common.domain.dto.EventDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NasaLogParser {

    /**
     * Regex Pattern for NASA KSC HTTP Logs (Common Log Format with HTTP Version).
     * Format: host - - [DD/MMM/YYYY:HH:MM:SS -0400] "METHOD PATH HTTP/1.0" STATUS
     * BYTES
     * Group 1: Source IP/Host
     * Group 2: Timestamp string
     * Group 3: HTTP Method
     * Group 4: Endpoint / Path
     * Group 5: HTTP Status Code
     * Group 6: Bytes (- or number)
     */
    private static final String LOG_PATTERN_REGEX = "^(\\S+) - - \\[(.+?)\\] \"(\\S+) (.*?)(?: HTTP/\\S+)?\" (\\d{3}) (\\d+|-).*$";
    private static final Pattern PATTERN = Pattern.compile(LOG_PATTERN_REGEX);

    // NASA log dates look like: 01/Jul/1995:00:00:01 -0400
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z",
            Locale.ENGLISH);

    /**
     * Parses a single line of log text into an EventDTO.
     * 
     * @param line raw text line
     * @return EventDTO or null if line is malformed
     */
    public EventDTO parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null; // Malformed line (Fail-safe parsing REQ-AG-02)
        }

        try {
            String sourceIp = matcher.group(1);
            String dateString = matcher.group(2);
            String method = matcher.group(3);
            String endpoint = matcher.group(4);
            int statusCode = Integer.parseInt(matcher.group(5));

            String bytesString = matcher.group(6);
            long bytes = bytesString.equals("-") ? 0L : Long.parseLong(bytesString);

            LocalDateTime timestamp = LocalDateTime.parse(dateString, DATE_FORMATTER);

            return EventDTO.builder()
                    .sourceIp(sourceIp)
                    .timestamp(timestamp)
                    .method(method)
                    .endpoint(endpoint)
                    .statusCode(statusCode)
                    .bytes(bytes)
                    .build();

        } catch (Exception e) {
            // Ignore parsing errors for individual fields and drop the line
            return null;
        }
    }
}
