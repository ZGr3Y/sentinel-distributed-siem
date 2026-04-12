package com.sentinel.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.common.domain.dto.DashboardSummaryDTO;
import com.sentinel.api.repository.DailyReportRepository;
import com.sentinel.common.domain.entity.DailyReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DailyReportRepository reportRepository;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReportService reportService;

    private DailyReport existingReport;
    private DashboardSummaryDTO mockSummary;

    @BeforeEach
    void setUp() {
        existingReport = new DailyReport(LocalDate.now(), "{\"date\": \"" + LocalDate.now().toString() + "\", \"status\": \"OK\"}");
        mockSummary = new DashboardSummaryDTO();
        mockSummary.setTotalAlerts(10L);
    }

    @Test
    void testGetDailyReport_WhenExists_ReturnsExistingReportData() {
        // Arrange
        when(reportRepository.findByReportDate(LocalDate.now())).thenReturn(Optional.of(existingReport));

        // Act
        String result = reportService.getDailyReport();

        // Assert
        assertEquals("{\"date\": \"" + LocalDate.now().toString() + "\", \"status\": \"OK\"}", result);
        verify(reportRepository, times(1)).findByReportDate(LocalDate.now());
        verifyNoInteractions(dashboardService);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void testGetDailyReport_WhenNotExists_GeneratesAndSavesNewReport() throws JsonProcessingException {
        // Arrange
        when(reportRepository.findByReportDate(LocalDate.now())).thenReturn(Optional.empty());
        when(dashboardService.getDashboardSummary()).thenReturn(mockSummary);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"new\": \"report data\"}");

        // Act
        String result = reportService.getDailyReport();

        // Assert
        assertEquals("{\"new\": \"report data\"}", result);
        verify(reportRepository, times(1)).findByReportDate(LocalDate.now());
        verify(dashboardService, times(1)).getDashboardSummary();
        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(reportRepository, times(1)).save(any(DailyReport.class));
    }

    @Test
    void testGetDailyReport_WhenJsonProcessingFails_ThrowsException() throws JsonProcessingException {
        // Arrange
        when(reportRepository.findByReportDate(LocalDate.now())).thenReturn(Optional.empty());
        when(dashboardService.getDashboardSummary()).thenReturn(mockSummary);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization mock error") {});

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> reportService.getDailyReport());
        assertTrue(exception.getMessage().contains("Serialization failed"));
        
        verify(reportRepository, never()).save(any(DailyReport.class));
    }

    @Test
    void testFallbackGetReport_ReturnsDegradedMessage() {
        // Arrange
        Exception exception = new RuntimeException("Database timeout");

        // Act
        String result = reportService.fallbackGetReport(exception);

        // Assert
        assertTrue(result.contains("DEGRADED"));
        assertTrue(result.contains("Reporting service is temporarily unavailable"));
    }
}
