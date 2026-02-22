package com.sentinel.api.controller;

import com.sentinel.api.service.DashboardService;
import com.sentinel.common.domain.dto.DashboardSummaryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Pattern: Remote Facade (1.10)
     * Provides a coarse-grained endpoint for clients to retrieve all dashboard KPI
     * metrics
     * in a single network call rather than individual queries.
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }
}
