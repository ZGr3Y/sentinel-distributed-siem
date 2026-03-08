package com.sentinel.common.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alerts_source_ip", columnList = "source_ip"),
        @Index(name = "idx_alerts_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class Alert {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "alert_type", nullable = false)
    private String type; // e.g., "DOS", "BRUTE_FORCE"

    @Column(name = "source_ip", nullable = false)
    private String sourceIp;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Convenience constructor that auto-generates ID and timestamp.
     */
    public Alert(String type, String sourceIp, String description) {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.type = type;
        this.sourceIp = sourceIp;
        this.description = description;
    }

    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
