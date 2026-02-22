package com.sentinel.common.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Pattern: Serialized LOB (1.11)
 * Stores a complex statistical graph as a single JSON object in the database,
 * avoiding complex relational mappings for historical archival data.
 */
@Entity
@Table(name = "daily_reports")
public class DailyReport {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    // We store the complex JSON structure in a single text column
    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportData;

    public DailyReport() {
        this.id = UUID.randomUUID().toString();
    }

    public DailyReport(LocalDate reportDate, String reportData) {
        this();
        this.reportDate = reportDate;
        this.reportData = reportData;
    }

    public String getId() {
        return id;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }
}
