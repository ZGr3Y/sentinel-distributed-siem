package com.sentinel.api.service;

import com.sentinel.common.domain.dto.request.BatchQueryRequest;
import com.sentinel.common.domain.dto.response.BatchQueryResponse;
import com.sentinel.common.domain.entity.Alert;
import com.sentinel.core.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InvestigationService {

    private final AlertRepository alertRepository;

    public InvestigationService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Pattern: Request Batch (1.15)
     * Processes a single network request containing multiple IP lookups.
     * Reduces chatty client-server behavior.
     *
     * Uses a single IN-clause query instead of N individual queries.
     */
    public BatchQueryResponse processBatchQuery(BatchQueryRequest request) {
        List<String> ips = request.getIpsToInvestigate();

        if (ips == null || ips.isEmpty()) {
            return new BatchQueryResponse(new HashMap<>(), 0);
        }

        // Single query for all IPs, then group results in memory
        List<Alert> allAlerts = alertRepository.findBySourceIpIn(ips);
        Map<String, List<Alert>> results = allAlerts.stream()
                .collect(Collectors.groupingBy(Alert::getSourceIp));

        // Ensure all queried IPs are present in the map (even with empty lists)
        for (String ip : ips) {
            results.putIfAbsent(ip, List.of());
        }

        return new BatchQueryResponse(results, ips.size());
    }
}
