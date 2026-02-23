package com.sentinel.api.controller;

import com.sentinel.api.service.SessionStateService;
import com.sentinel.common.domain.dto.request.DraftSaveRequest;
import com.sentinel.common.domain.entity.DraftState;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/draft")
public class DraftController {

    private final SessionStateService sessionStateService;

    public DraftController(SessionStateService sessionStateService) {
        this.sessionStateService = sessionStateService;
    }

    @PostMapping
    public ResponseEntity<?> saveDraft(@RequestBody DraftSaveRequest request) {
        UUID userId = getUserIdFromContext();
        DraftState draft = sessionStateService.saveDraft(userId, request.getPayload());
        return ResponseEntity.ok(draft);
    }

    @GetMapping
    public ResponseEntity<?> getDraft() {
        UUID userId = getUserIdFromContext();
        Optional<DraftState> draft = sessionStateService.getDraft(userId);

        if (draft.isPresent()) {
            return ResponseEntity.ok(draft.get());
        } else {
            return ResponseEntity.ok(Map.of("message", "No draft found for user"));
        }
    }

    private UUID getUserIdFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) auth.getPrincipal(); // Assuming principal is UUID based on JwtAuthenticationFilter
    }
}
