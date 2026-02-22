package com.sentinel.common.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "raw_events")
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

    @Column(name = "ingested_at")
    private LocalDateTime ingestedAt = LocalDateTime.now();

    public RawEvent() {
    }

    public RawEvent(Long id, String eventHash, String sourceIp, String requestPath, Integer statusCode, String severity,
            LocalDateTime ingestedAt) {
        this.id = id;
        this.eventHash = eventHash;
        this.sourceIp = sourceIp;
        this.requestPath = requestPath;
        this.statusCode = statusCode;
        this.severity = severity;
        this.ingestedAt = ingestedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventHash() {
        return eventHash;
    }

    public void setEventHash(String eventHash) {
        this.eventHash = eventHash;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(LocalDateTime ingestedAt) {
        this.ingestedAt = ingestedAt;
    }

    public static builder builder() {
        return new builder();
    }

    public static class builder {
        private Long id;
        private String eventHash;
        private String sourceIp;
        private String requestPath;
        private Integer statusCode;
        private String severity;
        private LocalDateTime ingestedAt = LocalDateTime.now();

        public builder id(Long id) {
            this.id = id;
            return this;
        }

        public builder eventHash(String eventHash) {
            this.eventHash = eventHash;
            return this;
        }

        public builder sourceIp(String sourceIp) {
            this.sourceIp = sourceIp;
            return this;
        }

        public builder requestPath(String requestPath) {
            this.requestPath = requestPath;
            return this;
        }

        public builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public builder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public builder ingestedAt(LocalDateTime ingestedAt) {
            this.ingestedAt = ingestedAt;
            return this;
        }

        public RawEvent build() {
            return new RawEvent(id, eventHash, sourceIp, requestPath, statusCode, severity, ingestedAt);
        }
    }
}
