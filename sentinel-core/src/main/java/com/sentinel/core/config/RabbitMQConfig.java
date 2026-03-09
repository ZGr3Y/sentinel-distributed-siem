package com.sentinel.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Core-specific RabbitMQ configuration.
 * All shared beans (exchange, queue, binding, JSON converter) are inherited
 * from
 * {@link com.sentinel.common.config.RabbitMQConfig} in sentinel-common.
 *
 * This class is kept as a placeholder for any core-specific RabbitMQ overrides.
 */
@Configuration
@Import(com.sentinel.common.config.RabbitMQConfig.class)
public class RabbitMQConfig {
    // Shared beans are automatically provided by sentinel-common's RabbitMQConfig.
    // Add core-specific RabbitMQ beans here if needed.
}
