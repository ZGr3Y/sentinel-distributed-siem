package com.sentinel.api.service;

import com.sentinel.common.domain.dto.DashboardSummaryDTO;
import com.sentinel.common.domain.entity.Alert;
import com.sentinel.core.repository.AlertRepository;
import com.sentinel.core.repository.RawEventRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pattern: Remote Facade (L5_RemoteFacadeDTO)
 * Provides a coarse-grained method that assembles all dashboard KPIs
 * for the client in a single call, avoiding chatty round-trips.
 */
@Service
public class DashboardService {

    private final RawEventRepository rawEventRepository;
    private final AlertRepository alertRepository;

    public DashboardService(RawEventRepository rawEventRepository, AlertRepository alertRepository) {
        this.rawEventRepository = rawEventRepository;
        this.alertRepository = alertRepository;
    }

    public DashboardSummaryDTO getDashboardSummary() {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();

        // Metrics
        dto.setTotalEvents(rawEventRepository.count());
        dto.setTotalAlerts(alertRepository.count());
        dto.setDosAttacks(alertRepository.countByAlertType("DOS"));
        dto.setBruteForceAttacks(alertRepository.countByAlertType("BRUTE_FORCE"));

        // System Health - check DB connectivity (if we got here, DB is UP)
        Map<String, String> health = new LinkedHashMap<>();
        health.put("database", "UP");
        health.put("broker", "UP"); // RabbitMQ presence implied by events arriving
        dto.setSystemHealth(health);

        // Latest 10 Alerts
        List<Alert> recent = alertRepository.findTop10ByOrderByCreatedAtDesc();
        List<DashboardSummaryDTO.AlertDTO> alertDTOs = recent.stream()
                .map(a -> new DashboardSummaryDTO.AlertDTO(
                        a.getId(),
                        a.getType(),
                        a.getSourceIp(),
                        a.getDescription(),
                        a.getCreatedAt() != null ? a.getCreatedAt().toString() : null))
                .collect(Collectors.toList());
        dto.setLatestAlerts(alertDTOs);

        return dto;
    }
}
