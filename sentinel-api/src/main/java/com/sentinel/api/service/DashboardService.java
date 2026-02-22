package com.sentinel.api.service;

import com.sentinel.common.domain.dto.DashboardSummaryDTO;
import com.sentinel.core.repository.AlertRepository;
import com.sentinel.core.repository.RawEventRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final RawEventRepository rawEventRepository;
    private final AlertRepository alertRepository;

    public DashboardService(RawEventRepository rawEventRepository, AlertRepository alertRepository) {
        this.rawEventRepository = rawEventRepository;
        this.alertRepository = alertRepository;
    }

    public DashboardSummaryDTO getDashboardSummary() {
        long totalEvents = rawEventRepository.count();
        long totalAlerts = alertRepository.count();
        long dosAttacks = alertRepository.countByAlertType("DOS");
        long bruteForceAttacks = alertRepository.countByAlertType("BRUTE_FORCE");

        return new DashboardSummaryDTO(totalEvents, totalAlerts, dosAttacks, bruteForceAttacks);
    }
}
