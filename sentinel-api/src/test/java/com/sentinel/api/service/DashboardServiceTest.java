package com.sentinel.api.service;

import com.sentinel.common.domain.dto.DashboardSummaryDTO;
import com.sentinel.common.domain.entity.Alert;
import com.sentinel.core.repository.AlertRepository;
import com.sentinel.core.repository.RawEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private RawEventRepository rawEventRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Alert alert1;
    private Alert alert2;

    @BeforeEach
    void setUp() {
        alert1 = new Alert("DOS", "192.168.1.1", "High traffic DOS");
        alert1.setId("al-1");
        alert1.setCreatedAt(LocalDateTime.now());

        alert2 = new Alert("BRUTE_FORCE", "10.0.0.9", "Failed login multiple times");
        alert2.setId("al-2");
        alert2.setCreatedAt(LocalDateTime.now().minusMinutes(5));
    }

    @Test
    void testGetDashboardSummary_ReturnsAggregatedData() {
        // Arrange
        Mockito.when(rawEventRepository.count()).thenReturn(1500L);
        Mockito.when(alertRepository.count()).thenReturn(200L);
        Mockito.when(alertRepository.countByAlertType("DOS")).thenReturn(150L);
        Mockito.when(alertRepository.countByAlertType("BRUTE_FORCE")).thenReturn(50L);
        
        List<Alert> recentAlerts = Arrays.asList(alert1, alert2);
        Mockito.when(alertRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(recentAlerts);

        // Act
        DashboardSummaryDTO result = dashboardService.getDashboardSummary();

        // Assert
        assertNotNull(result);
        assertEquals(1500L, result.getTotalEvents());
        assertEquals(200L, result.getTotalAlerts());
        assertEquals(150L, result.getDosAttacks());
        assertEquals(50L, result.getBruteForceAttacks());
        
        assertNotNull(result.getSystemHealth());
        assertEquals("UP", result.getSystemHealth().get("database"));
        assertEquals("UP", result.getSystemHealth().get("broker"));

        assertNotNull(result.getLatestAlerts());
        assertEquals(2, result.getLatestAlerts().size());
        assertEquals("al-1", result.getLatestAlerts().get(0).getId());
        assertEquals("DOS", result.getLatestAlerts().get(0).getType());

        Mockito.verify(rawEventRepository, times(1)).count();
        Mockito.verify(alertRepository, times(1)).count();
        Mockito.verify(alertRepository, times(1)).countByAlertType("DOS");
        Mockito.verify(alertRepository, times(1)).countByAlertType("BRUTE_FORCE");
        Mockito.verify(alertRepository, times(1)).findTop10ByOrderByCreatedAtDesc();
    }
}
