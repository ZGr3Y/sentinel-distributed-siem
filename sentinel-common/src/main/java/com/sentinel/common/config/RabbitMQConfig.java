package com.sentinel.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Shared RabbitMQ configuration for all Sentinel modules.
 * Defines the exchange, queue, binding, and JSON converter beans.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "sentinel.direct";

    @Value("${sentinel.queue.ingress}")
    private String ingressQueueName;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue ingressQueue() {
        return new Queue(ingressQueueName, true); // durable
    }

    @Bean
    public Binding binding(Queue ingressQueue, DirectExchange exchange) {
        return BindingBuilder.bind(ingressQueue).to(exchange).with(ingressQueueName);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
