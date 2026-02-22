package com.sentinel.api.service;

import com.sentinel.common.domain.dto.request.BatchQueryRequest;
import com.sentinel.common.domain.dto.response.BatchQueryResponse;
import com.sentinel.common.domain.entity.Alert;
import com.sentinel.core.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     */
    public BatchQueryResponse processBatchQuery(BatchQueryRequest request) {
        Map<String, List<Alert>> results = new HashMap<>();
        List<String> ips = request.getIpsToInvestigate();

        if (ips != null && !ips.isEmpty()) {
            for (String ip : ips) {
                // In a true highly optimized environment, this might be heavily parallelized
                // or use a single IN clause: alertRepository.findBySourceIpIn(ips)
                List<Alert> ipAlerts = alertRepository.findBySourceIp(ip);
                results.put(ip, ipAlerts);
            }
        }

        return new BatchQueryResponse(results, ips != null ? ips.size() : 0);
    }
}
