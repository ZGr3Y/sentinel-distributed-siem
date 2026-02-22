package com.sentinel.common.domain.dto;

import java.time.LocalDateTime;

public class EventDTO {

    private String eventId; // The SHA-256 Hash
    private String sourceIp;
    private LocalDateTime timestamp;
    private String method;
    private String endpoint;
    private Integer statusCode;
    private Long bytes;

    public EventDTO() {
    }

    public EventDTO(String eventId, String sourceIp, LocalDateTime timestamp, String method, String endpoint,
            Integer statusCode, Long bytes) {
        this.eventId = eventId;
        this.sourceIp = sourceIp;
        this.timestamp = timestamp;
        this.method = method;
        this.endpoint = endpoint;
        this.statusCode = statusCode;
        this.bytes = bytes;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public static builder builder() {
        return new builder();
    }

    public static class builder {
        private String eventId;
        private String sourceIp;
        private LocalDateTime timestamp;
        private String method;
        private String endpoint;
        private Integer statusCode;
        private Long bytes;

        public builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public builder sourceIp(String sourceIp) {
            this.sourceIp = sourceIp;
            return this;
        }

        public builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public builder method(String method) {
            this.method = method;
            return this;
        }

        public builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public builder bytes(Long bytes) {
            this.bytes = bytes;
            return this;
        }

        public EventDTO build() {
            return new EventDTO(eventId, sourceIp, timestamp, method, endpoint, statusCode, bytes);
        }
    }
}
