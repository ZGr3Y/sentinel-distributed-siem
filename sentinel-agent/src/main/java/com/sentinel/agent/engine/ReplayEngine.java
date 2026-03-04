package com.sentinel.agent.engine;

import com.sentinel.agent.parser.NasaLogParser;
import com.sentinel.agent.producer.LogProducer;
import com.sentinel.common.domain.dto.EventDTO;
import com.sentinel.common.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ReplayEngine {

    private static final Logger log = LoggerFactory.getLogger(ReplayEngine.class);

    private final NasaLogParser parser;
    private final LogProducer producer;

    @Value("classpath:data/access_log_Jul95")
    private Resource logFile;

    @Value("${sentinel.agent.speedup:1}")
    private int speedupFactor;

    public ReplayEngine(NasaLogParser parser, LogProducer producer) {
        this.parser = parser;
        this.producer = producer;
    }

    @Value("${sentinel.agent.mode:replay}")
    private String agentMode;

    @EventListener(ApplicationReadyEvent.class)
    public void startReplay() {
        if (!"replay".equalsIgnoreCase(agentMode)) {
            log.info("Agent mode is '{}'. ReplayEngine is disabled.", agentMode);
            return;
        }

        log.info("Starting Time-Shifted Replay Engine with speedup factor {}x", speedupFactor);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(logFile.getInputStream()))) {
            String line;
            LocalDateTime lastLogTime = null;

            while ((line = reader.readLine()) != null) {
                EventDTO event = parser.parseLine(line);

                if (event == null) {
                    continue; // Skip malformed lines
                }

                // Time-Shift Algorithm
                if (lastLogTime != null) {
                    long delayMillis = ChronoUnit.MILLIS.between(lastLogTime, event.getTimestamp());

                    if (delayMillis > 0) {
                        long sleepTime = delayMillis / speedupFactor;
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.warn("Replay interrupted");
                            break;
                        }
                    }
                }

                lastLogTime = event.getTimestamp();

                // Generate Event ID (Hash)
                event.setEventId(HashUtils.calculateEventHash(event));

                // Update Timestamp to CURRENT REPLAY TIME
                // (Crucial for Sliding Window Analytics down the pipeline)
                event.setTimestamp(LocalDateTime.now());

                // Send to RabbitMQ
                producer.sendEvent(event);
            }
            log.info("Replay completed. End of dataset.");

        } catch (Exception e) {
            log.error("Failed to read log file or process replay: {}", e.getMessage(), e);
        }
    }
}
