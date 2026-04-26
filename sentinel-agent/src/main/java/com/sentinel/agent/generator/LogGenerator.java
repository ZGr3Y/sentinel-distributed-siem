package com.sentinel.agent.generator;

import com.sentinel.common.domain.dto.EventDTO;

public interface LogGenerator {
    /**
     * Generates a realistic log event.
     *
     * @return a realistically populated EventDTO instance
     */
    EventDTO generateLog();
}
