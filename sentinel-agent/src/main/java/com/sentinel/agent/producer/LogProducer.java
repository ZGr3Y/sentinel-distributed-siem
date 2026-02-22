package com.sentinel.agent.producer;

import com.sentinel.common.domain.dto.EventDTO;
import com.sentinel.agent.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogProducer {

    private static final Logger log = LoggerFactory.getLogger(LogProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public LogProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${sentinel.queue.ingress}")
    private String routingKey;

    public void sendEvent(EventDTO event) {
        if (event == null)
            return;

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
            log.debug("Sent event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to send event to RabbitMQ: {}", e.getMessage());
        }
    }
}
