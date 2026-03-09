package com.sentinel.agent.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Agent-specific RabbitMQ configuration.
 * Shared beans (exchange, queue, binding, JSON converter) are inherited from
 * {@link com.sentinel.common.config.RabbitMQConfig} in sentinel-common.
 */
@Configuration
@Import(com.sentinel.common.config.RabbitMQConfig.class)
public class RabbitMQConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
