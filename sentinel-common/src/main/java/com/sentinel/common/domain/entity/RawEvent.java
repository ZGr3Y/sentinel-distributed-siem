package com.sentinel.common.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_events", indexes = {
        @Index(name = "idx_raw_events_source_ip", columnList = "source_ip"),
        @Index(name = "idx_raw_events_ingested_at", columnList = "ingested_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_hash", unique = true, nullable = false, length = 64)
    private String eventHash;

    @Column(name = "source_ip", nullable = false, length = 45)
    private String sourceIp;

    @Column(name = "request_path", columnDefinition = "TEXT")
    private String requestPath;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "severity", length = 10)
    private String severity;

    @Builder.Default
    @Column(name = "ingested_at")
    private LocalDateTime ingestedAt = LocalDateTime.now();
}
