package com.sentinel.common.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pattern: Session State Server-Side (L6_SessionStateSLOB)
 * Persists user work-in-progress (draft) data to the database,
 * keyed by the user's ID extracted from the JWT token.
 */
@Entity
@Table(name = "draft_states")
@Getter
@Setter
@NoArgsConstructor
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

    public DraftState(String userId, String draftPayload) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.draftPayload = draftPayload;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
