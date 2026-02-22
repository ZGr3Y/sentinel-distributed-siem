package com.sentinel.common.domain.dto;

public class DashboardSummaryDTO {

    private long totalEvents;
    private long totalAlerts;
    private long dosAttacks;
    private long bruteForceAttacks;

    public DashboardSummaryDTO() {
    }

    public DashboardSummaryDTO(long totalEvents, long totalAlerts, long dosAttacks, long bruteForceAttacks) {
        this.totalEvents = totalEvents;
        this.totalAlerts = totalAlerts;
        this.dosAttacks = dosAttacks;
        this.bruteForceAttacks = bruteForceAttacks;
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
}
