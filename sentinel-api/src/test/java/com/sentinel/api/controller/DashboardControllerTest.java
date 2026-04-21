package com.sentinel.api.controller;

import com.sentinel.api.service.DashboardService;
import com.sentinel.common.domain.dto.DashboardSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    private DashboardController controller;

    @BeforeEach
    void setUp() {
        controller = new DashboardController(dashboardService);
    }

    @Test
    void testGetSummary_returnsSummarySuccessfully() {
        DashboardSummaryDTO mockSummary = new DashboardSummaryDTO();
        // Assuming some default constructor or setters, but returning non-null is enough
        
        when(dashboardService.getDashboardSummary()).thenReturn(mockSummary);

        ResponseEntity<DashboardSummaryDTO> response = controller.getSummary();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockSummary, response.getBody());
    }
}
