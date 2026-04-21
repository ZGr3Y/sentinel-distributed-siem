package com.sentinel.core.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.sentinel.common.domain.dto.EventDTO;
import com.sentinel.common.domain.entity.RawEvent;
import java.util.UUID;
import com.sentinel.core.repository.RawEventRepository;

@Service
public class EventConsumerService {

    private static final Logger log = LoggerFactory.getLogger(EventConsumerService.class);
    private final RawEventRepository repository;
    private final AnalyticsService analyticsService;
    private final Executor analyticsExecutor;

    @Autowired
    public EventConsumerService(RawEventRepository repository, AnalyticsService analyticsService,
            @Qualifier("analyticsExecutor") Executor analyticsExecutor) {
        this.repository = repository;
        this.analyticsService = analyticsService;
        this.analyticsExecutor = analyticsExecutor;
    }

    @RabbitListener(queues = "${sentinel.queue.ingress}")
    public void consumeEvent(EventDTO dto, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // 1. Indempotency Check: Trust the native UUID provided by the Agent
            String rawEventId = dto.getEventId();
            final String eventHash = (rawEventId == null || rawEventId.isEmpty()) 
                                     ? UUID.randomUUID().toString() 
                                     : rawEventId;

            // 2. Map DTO to Entity
            RawEvent event = new RawEvent();
            event.setEventHash(eventHash);
            event.setSourceIp(dto.getSourceIp());
            event.setIngestedAt(LocalDateTime.now());
            // Map DTO endpoint to RawEvent requestPath
            event.setRequestPath(dto.getEndpoint());
            event.setStatusCode(dto.getStatusCode());

            // 3. Severity Classification Rule Engine
            String severity = classifySeverity(dto);
            event.setSeverity(severity);
            dto.setSeverity(severity); // Attach it to DTO for the AnalyticsService

            // 4. Persist to PostgreSQL (Idempotency enforced by DB constraints)
            repository.save(event);
            log.info("Processed and saved event: {}", eventHash);

            // 5. Analytics & Threat Detection (Non-blocking, Asynchronous)
            // Manual Ack is sent ONLY AFTER analytics completion to ensure Zero Data Loss.
            CompletableFuture.runAsync(() -> {
                try {
                    analyticsService.analyzeEvent(dto);
                    // SUCCESS: Acknowledge the message (it will be removed from queue)
                    channel.basicAck(deliveryTag, false);
                } catch (Exception e) {
                    log.error("Error during asynchronous analytics processing for event: {}. Requeuing.", eventHash, e);
                    try {
                        // FAILURE: Nack and requeue so another consumer (or this one) can try again
                        channel.basicNack(deliveryTag, false, true);
                    } catch (Exception nackEx) {
                        log.error("Failed to send NACK for event: {}", eventHash, nackEx);
                    }
                }
            }, analyticsExecutor);

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate event discarded: {}. Sending Ack to clear queue.", dto.getEventId());
            try {
                // Duplicate at DB level means it was already processed, safe to Ack.
                channel.basicAck(deliveryTag, false);
            } catch (Exception ackEx) {
                log.error("Failed to send Ack for duplicate event: {}", dto.getEventId(), ackEx);
            }
        } catch (Exception e) {
            log.error("Critical error processing event: {}. Requesting requeue.", e.getMessage(), e);
            try {
                // Unexpected error: requeue to prevent data loss.
                channel.basicNack(deliveryTag, false, true);
            } catch (Exception nackEx) {
                log.error("Failed to send NACK after exception", nackEx);
            }
        }
    }

    private String classifySeverity(EventDTO dto) {
        // CRITICAL Rule: Path Traversal or Command Execution attempts
        if (dto.getEndpoint() != null
                && dto.getEndpoint().matches("(?i)(.*\\.\\..*|.*/etc/passwd.*|.*cmd\\.exe.*|.*/bin/sh.*)")) {
            return "CRITICAL";
        }

        // WARNING Rule: HTTP Client/Server Errors
        if (dto.getStatusCode() != null && dto.getStatusCode() >= 400) {
            return "WARNING";
        }

        // INFO Rule: Default status
        return "INFO";
    }
}
