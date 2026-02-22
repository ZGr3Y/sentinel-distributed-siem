package com.sentinel.api.controller;

import com.sentinel.api.service.InvestigationService;
import com.sentinel.common.domain.dto.request.BatchQueryRequest;
import com.sentinel.common.domain.dto.response.BatchQueryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/investigation")
public class InvestigationController {

    private final InvestigationService investigationService;

    public InvestigationController(InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    /**
     * Pattern: Request Batch (1.15)
     * Allows fetching history for up to N multiple IP addresses in a single HTTP
     * POST request.
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchQueryResponse> batchQuery(@RequestBody BatchQueryRequest batchRequest) {
        if (batchRequest == null || batchRequest.getIpsToInvestigate() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(investigationService.processBatchQuery(batchRequest));
    }
}
