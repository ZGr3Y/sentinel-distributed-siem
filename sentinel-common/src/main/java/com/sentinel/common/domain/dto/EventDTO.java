package com.sentinel.common.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTO {

    private String eventId; // The SHA-256 Hash
    private String sourceIp;
    private LocalDateTime timestamp;
    private String method;
    private String endpoint;
    private Integer statusCode;
    private Long bytes;
    private String severity;
}
