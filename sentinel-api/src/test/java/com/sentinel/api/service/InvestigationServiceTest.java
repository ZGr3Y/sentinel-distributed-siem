package com.sentinel.api.service;

import com.sentinel.common.domain.dto.request.BatchQueryRequest;
import com.sentinel.common.domain.dto.response.BatchQueryResponse;
import com.sentinel.common.domain.entity.Alert;
import com.sentinel.core.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestigationServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private InvestigationService investigationService;

    private Alert alert1;
    private Alert alert2;

    @BeforeEach
    void setUp() {
        alert1 = new Alert("DOS", "192.168.1.100", "High traffic");
        alert2 = new Alert("BRUTE_FORCE", "10.0.0.5", "Failed logins");
    }

    @Test
    void testProcessBatchQuery_WithEmptyList_ReturnsEmptyResponse() {
        // Arrange
        BatchQueryRequest request = new BatchQueryRequest(Collections.emptyList());

        // Act
        BatchQueryResponse response = investigationService.processBatchQuery(request);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalIpsQueried());
        assertTrue(response.getIpAlertsMap().isEmpty());
        verifyNoInteractions(alertRepository);
    }

    @Test
    void testProcessBatchQuery_WithNullList_ReturnsEmptyResponse() {
        // Arrange
        BatchQueryRequest request = new BatchQueryRequest(null);

        // Act
        BatchQueryResponse response = investigationService.processBatchQuery(request);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalIpsQueried());
        assertTrue(response.getIpAlertsMap().isEmpty());
        verifyNoInteractions(alertRepository);
    }

    @Test
    void testProcessBatchQuery_WithValidIps_ReturnsGroupedResults() {
        // Arrange
        List<String> ips = Arrays.asList("192.168.1.100", "10.0.0.5", "127.0.0.1");
        BatchQueryRequest request = new BatchQueryRequest(ips);

        when(alertRepository.findBySourceIpIn(ips)).thenReturn(Arrays.asList(alert1, alert2));

        // Act
        BatchQueryResponse response = investigationService.processBatchQuery(request);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.getTotalIpsQueried());
        assertEquals(3, response.getIpAlertsMap().size());

        // IPs with alerts
        assertEquals(1, response.getIpAlertsMap().get("192.168.1.100").size());
        assertEquals("DOS", response.getIpAlertsMap().get("192.168.1.100").get(0).getType());

        assertEquals(1, response.getIpAlertsMap().get("10.0.0.5").size());
        assertEquals("BRUTE_FORCE", response.getIpAlertsMap().get("10.0.0.5").get(0).getType());

        // IP with no alerts should be present but empty
        assertTrue(response.getIpAlertsMap().get("127.0.0.1").isEmpty());

        verify(alertRepository, times(1)).findBySourceIpIn(ips);
    }
}
