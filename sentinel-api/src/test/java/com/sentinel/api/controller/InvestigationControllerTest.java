package com.sentinel.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.api.service.InvestigationService;
import com.sentinel.common.domain.dto.request.BatchQueryRequest;
import com.sentinel.common.domain.dto.response.BatchQueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InvestigationController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvestigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvestigationService investigationService;

    private BatchQueryRequest validRequest;
    private BatchQueryResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = new BatchQueryRequest(Arrays.asList("192.168.1.1", "10.0.0.2"));
        mockResponse = new BatchQueryResponse(Collections.emptyMap(), 2);
    }

    @Test
    void testBatchQuery_Success() throws Exception {
        Mockito.when(investigationService.processBatchQuery(any(BatchQueryRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/investigation/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedCount").value(2));
    }

    @Test
    void testBatchQuery_NullRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/investigation/batch")
                        .contentType(MediaType.APPLICATION_JSON)) // Missing body
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBatchQuery_NullIpsList_ReturnsBadRequest() throws Exception {
        BatchQueryRequest invalidRequest = new BatchQueryRequest(null);

        mockMvc.perform(post("/api/investigation/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
