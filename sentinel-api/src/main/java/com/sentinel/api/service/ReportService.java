package com.sentinel.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.api.repository.DailyReportRepository;
import com.sentinel.common.domain.entity.DailyReport;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final DailyReportRepository reportRepository;
    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    public ReportService(DailyReportRepository reportRepository, DashboardService dashboardService,
            ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.dashboardService = dashboardService;
        this.objectMapper = objectMapper;
    }

    /**
     * Pattern: Circuit Breaker (1.16 / Resilience4J)
     * AND Pattern: Serialized LOB (1.11)
     *
     * This method retrieves a report. If a daily report doesn't exist for today,
     * it generates a complex statistical object graph, serializes it to JSON (LOB),
     * and saves it.
     * The database operation is wrapped in a Circuit Breaker.
     * If the DB connection fails or times out repeatedly, the circuit opens and
     * fallbackGetReport is called.
     */
    @CircuitBreaker(name = "reportService", fallbackMethod = "fallbackGetReport")
    @Transactional
    public String getDailyReport() {
        LocalDate today = LocalDate.now();

        // Check if we already have the Serialized LOB for today
        Optional<DailyReport> existingReport = reportRepository.findByReportDate(today);
        if (existingReport.isPresent()) {
            log.info("Returning existing Serialized LOB report for {}", today);
            return existingReport.get().getReportData();
        }

        log.info("Generating new Serialized LOB report for {}", today);
        // Simulate heavy aggregation using our remote facade DTO logic
        Map<String, Object> stats = new HashMap<>();
        stats.put("date", today.toString());
        stats.put("metrics", dashboardService.getDashboardSummary());
        stats.put("systemStatus", "OPERATIONAL");
        stats.put("notes", "Aggregated daily summary.");

        try {
            // Serialize to JSON (Serialized LOB Pattern)
            String jsonReport = objectMapper.writeValueAsString(stats);

            DailyReport newReport = new DailyReport(today, jsonReport);
            reportRepository.save(newReport);

            return jsonReport;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize LOB report", e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    /**
     * Fallback method triggered when the Circuit Breaker is OPEN.
     */
    public String fallbackGetReport(Exception e) {
        log.warn("🔄 Circuit Breaker OPEN! Fallback activated for getDailyReport(). Reason: {}", e.getMessage());
        return "{\"status\": \"DEGRADED\", \"message\": \"Reporting service is temporarily unavailable due to high load or database disconnect. Please try again later.\"}";
    }
}
