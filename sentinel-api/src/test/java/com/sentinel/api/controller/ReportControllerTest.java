package com.sentinel.api.controller;

import com.sentinel.api.service.ReportService;
import com.sentinel.api.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void testGetDailyReport_ReturnsOk() throws Exception {
        String jsonReport = "{\"date\":\"2026-04-11\", \"status\":\"OK\"}";
        Mockito.when(reportService.getDailyReport()).thenReturn(jsonReport);

        mockMvc.perform(get("/api/reports/daily")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(jsonReport));
    }
}
