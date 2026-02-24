package com.sentinel.api.controller;

import com.sentinel.api.service.SessionStateService;
import com.sentinel.common.domain.dto.request.DraftSaveRequest;
import com.sentinel.common.domain.entity.DraftState;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;

/**
 * Pattern: Session State Server-Side (L6_SessionStateSLOB)
 * Persists user work-in-progress to the database, keyed by userId extracted
 * from the JWT.
 * The session data is NOT stored in the token itself (REQ-SEC-02).
 */
@RestController
@RequestMapping("/api/draft")
public class DraftController {

    private final SessionStateService sessionStateService;

    public DraftController(SessionStateService sessionStateService) {
        this.sessionStateService = sessionStateService;
    }

    @PostMapping
    public ResponseEntity<?> saveDraft(@RequestBody DraftSaveRequest request) {
        String userId = getUserIdFromContext();
        DraftState draft = sessionStateService.saveDraft(userId, request.getPayload());
        return ResponseEntity.ok(draft);
    }

    @GetMapping
    public ResponseEntity<?> getDraft() {
        String userId = getUserIdFromContext();
        Optional<DraftState> draft = sessionStateService.getDraft(userId);

        if (draft.isPresent()) {
            return ResponseEntity.ok(draft.get());
        } else {
            return ResponseEntity.ok(Map.of("message", "No draft found for user"));
        }
    }

    private String getUserIdFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Principal is the userId String (set by JwtAuthenticationFilter from JWT
        // subject)
        return (String) auth.getPrincipal();
    }
}
