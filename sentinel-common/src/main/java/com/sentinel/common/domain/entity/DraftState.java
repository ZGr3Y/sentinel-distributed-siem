package com.sentinel.common.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pattern: Session State Server-Side (L6_SessionStateSLOB)
 * Persists user work-in-progress (draft) data to the database,
 * keyed by the user's ID extracted from the JWT token.
 */
@Entity
@Table(name = "draft_states")
public class DraftState {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String draftPayload;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public DraftState() {
        this.id = UUID.randomUUID().toString();
    }

    public DraftState(String userId, String draftPayload) {
        this();
        this.userId = userId;
        this.draftPayload = draftPayload;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDraftPayload() {
        return draftPayload;
    }

    public void setDraftPayload(String draftPayload) {
        this.draftPayload = draftPayload;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
