package com.sentinel.common.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Pattern: Serialized LOB (1.11)
 * Stores a complex statistical graph as a single JSON object in the database,
 * avoiding complex relational mappings for historical archival data.
 */
@Entity
@Table(name = "daily_reports")
@Getter
@Setter
@NoArgsConstructor
public class DailyReport {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportData;

    public DailyReport(LocalDate reportDate, String reportData) {
        this.id = UUID.randomUUID().toString();
        this.reportDate = reportDate;
        this.reportData = reportData;
    }
}
