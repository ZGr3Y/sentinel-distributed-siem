package com.sentinel.common.domain.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Pattern: Remote Facade + DTO (L5_RemoteFacadeDTO)
 * Coarse-grained DTO providing all dashboard data in a single network call.
 *
 * Fulfills REQ-API-01 from the SRS:
 * - System health (UP/DOWN) of DB and RabbitMQ
 * - Event/alert counts
 * - Latest alerts list
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    private Map<String, String> systemHealth;
    private long totalEvents;
    private long totalAlerts;
    private long dosAttacks;
    private long bruteForceAttacks;
    private List<AlertDTO> latestAlerts;

    /**
     * Nested DTO for alert data in the dashboard summary.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertDTO {
        private String id;
        private String type;
        private String sourceIp;
        private String description;
        private String createdAt;
    }
}
