package com.sentinel.api.controller;

import com.sentinel.api.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Triggers the generation or retrieval of the Serialized LOB daily report.
     * Protected by a Circuit Breaker.
     */
    @GetMapping("/daily")
    public ResponseEntity<String> getDailyReport() {
        return ResponseEntity.ok(reportService.getDailyReport());
    }
}
