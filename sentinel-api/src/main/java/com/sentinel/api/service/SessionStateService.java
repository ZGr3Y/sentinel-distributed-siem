package com.sentinel.api.service;

import com.sentinel.api.repository.DraftStateRepository;
import com.sentinel.common.domain.entity.DraftState;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Pattern: Session State Server-Side (L6_SessionStateSLOB)
 * Manages persisting and retrieving user drafts from the database.
 */
@Service
public class SessionStateService {

    private final DraftStateRepository repository;

    public SessionStateService(DraftStateRepository repository) {
        this.repository = repository;
    }

    public DraftState saveDraft(String userId, String payload) {
        Optional<DraftState> existing = repository.findByUserId(userId);
        if (existing.isPresent()) {
            DraftState state = existing.get();
            state.setDraftPayload(payload);
            state.setUpdatedAt(LocalDateTime.now());
            return repository.save(state);
        } else {
            DraftState newState = new DraftState(userId, payload);
            return repository.save(newState);
        }
    }

    public Optional<DraftState> getDraft(String userId) {
        return repository.findByUserId(userId);
    }
}
