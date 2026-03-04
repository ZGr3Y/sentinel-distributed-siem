package com.sentinel.agent.engine;

import com.sentinel.agent.generator.LogGenerator;
import com.sentinel.agent.producer.LogProducer;
import com.sentinel.common.domain.dto.EventDTO;
import com.sentinel.common.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class GeneratorEngine {

    private static final Logger log = LoggerFactory.getLogger(GeneratorEngine.class);

    private final LogGenerator logGenerator;
    private final LogProducer producer;

    @Value("${sentinel.agent.mode:replay}")
    private String agentMode;

    @Value("${sentinel.agent.generator.eps:10}")
    private int eventsPerSecond;

    public GeneratorEngine(LogGenerator logGenerator, LogProducer producer) {
        this.logGenerator = logGenerator;
        this.producer = producer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startGeneration() {
        if (!"generate".equalsIgnoreCase(agentMode)) {
            log.info("Agent mode is '{}'. GeneratorEngine is disabled.", agentMode);
            return;
        }

        log.info("Starting Log Generator Engine at {} EPS", eventsPerSecond);
        long delayMillis = 1000 / Math.max(1, eventsPerSecond);

        Thread generatorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    EventDTO event = logGenerator.generateLog();
                    event.setEventId(HashUtils.calculateEventHash(event));

                    producer.sendEvent(event);

                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Generator Engine interrupted");
                    break;
                } catch (Exception e) {
                    log.error("Error during log generation: {}", e.getMessage(), e);
                }
            }
        });

        generatorThread.setName("LogGeneratorThread");
        generatorThread.setDaemon(true); // Don't block application shutdown
        generatorThread.start();
    }
}
