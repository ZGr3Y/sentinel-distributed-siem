package com.sentinel.common.domain.dto;

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
public class DashboardSummaryDTO {

    private Map<String, String> systemHealth;
    private long totalEvents;
    private long totalAlerts;
    private long dosAttacks;
    private long bruteForceAttacks;
    private List<AlertDTO> latestAlerts;

    public DashboardSummaryDTO() {
    }

    public Map<String, String> getSystemHealth() {
        return systemHealth;
    }

    public void setSystemHealth(Map<String, String> systemHealth) {
        this.systemHealth = systemHealth;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public long getDosAttacks() {
        return dosAttacks;
    }

    public void setDosAttacks(long dosAttacks) {
        this.dosAttacks = dosAttacks;
    }

    public long getBruteForceAttacks() {
        return bruteForceAttacks;
    }

    public void setBruteForceAttacks(long bruteForceAttacks) {
        this.bruteForceAttacks = bruteForceAttacks;
    }

    public List<AlertDTO> getLatestAlerts() {
        return latestAlerts;
    }

    public void setLatestAlerts(List<AlertDTO> latestAlerts) {
        this.latestAlerts = latestAlerts;
    }

    /**
     * Nested DTO for alert data in the dashboard summary.
     */
    public static class AlertDTO {
        private String id;
        private String type;
        private String sourceIp;
        private String description;
        private String createdAt;

        public AlertDTO() {
        }

        public AlertDTO(String id, String type, String sourceIp, String description, String createdAt) {
            this.id = id;
            this.type = type;
            this.sourceIp = sourceIp;
            this.description = description;
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSourceIp() {
            return sourceIp;
        }

        public void setSourceIp(String sourceIp) {
            this.sourceIp = sourceIp;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
