package com.sentinel.common.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "draft_states")
public class DraftState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String draftPayload;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public DraftState() {
    }

    public DraftState(UUID userId, String draftPayload) {
        this.userId = userId;
        this.draftPayload = draftPayload;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
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
