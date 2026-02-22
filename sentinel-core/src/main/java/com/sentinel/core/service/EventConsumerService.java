package com.sentinel.core.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.sentinel.common.domain.dto.EventDTO;
import com.sentinel.common.domain.entity.RawEvent;
import com.sentinel.common.util.HashUtils;
import com.sentinel.core.repository.RawEventRepository;

@Service
public class EventConsumerService {

    private static final Logger log = LoggerFactory.getLogger(EventConsumerService.class);
    private final RawEventRepository repository;

    @Autowired
    public EventConsumerService(RawEventRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "${sentinel.queue.ingress}")
    public void consumeEvent(EventDTO dto) {
        try {
            // 1. Indempotency Check: Recalculate hash to ensure data integrity
            String eventHash = HashUtils.calculateEventHash(dto);

            // 2. Map DTO to Entity
            RawEvent event = new RawEvent();
            event.setEventHash(eventHash);
            event.setSourceIp(dto.getSourceIp());
            event.setIngestedAt(LocalDateTime.now());
            // Map DTO endpoint to RawEvent requestPath
            event.setRequestPath(dto.getEndpoint());
            event.setStatusCode(dto.getStatusCode());
            // Note: method and bytes are not mapped because RawEvent entity
            // doesn't have them based on the current schema in common module

            // 3. Severity Classification Rule Engine
            event.setSeverity(classifySeverity(dto));

            // 4. Persist to PostgreSQL (Idempotency enforced by DB constraints)
            repository.save(event);
            log.info("Processed and saved event: {}", eventHash);

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate event discarded: {}. Cause: {}", dto.getEventId(),
                    e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage(), e);
            throw e; // Nack message back to queue
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
