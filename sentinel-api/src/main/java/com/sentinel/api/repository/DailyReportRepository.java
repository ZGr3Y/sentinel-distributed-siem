package com.sentinel.api.repository;

import com.sentinel.common.domain.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, String> {
    Optional<DailyReport> findByReportDate(LocalDate reportDate);
}
