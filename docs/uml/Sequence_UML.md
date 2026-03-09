# Sentinel SIEM - Sequence Diagrams

Shows the chronological interaction between components.

## 1. Event Ingestion Sequence

```mermaid
sequenceDiagram
    participant Agent as Sentinel Agent
    participant RMQ as RabbitMQ (sentinel.direct)
    participant Core as Sentinel Core
    participant DB as PostgreSQL (raw_events)
    participant Async as Async Executor
    participant AlertDB as PostgreSQL (alerts)

    Agent->>Agent: Read & Parse Log Line
    Agent->>RMQ: basicPublish(EventDTO)
    RMQ-->>Core: @RabbitListener Delivery
    
    Core->>Core: Re-hash & Classify Severity
    Core->>DB: repository.save(RawEvent)
    
    alt UNIQUE constraint violated
        DB-->>Core: DataIntegrityViolationException
        Core->>Core: log.warn("Duplicate discarded")
    else New Event
        DB-->>Core: Saved OK
        Core->>Async: runAsync(AnalyticsService.analyzeEvent)
        
        par Threat Detection
            Async->>Async: Evaluate DOS RateLimiter
        and
            Async->>Async: Evaluate Brute Force RateLimiter
        and
            Async->>Async: Evaluate Payload Signatures
        end
        
        alt Threshold Exceeded
            Async->>AlertDB: repository.save(Alert)
        end
    end
```

## 2. Authentication Sequence (Login)

```mermaid
sequenceDiagram
    actor Admin
    participant UI as React Dashboard
    participant API as Sentinel API
    participant DB as PostgreSQL (users)
    participant Crypto as SCryptUtil
    participant JWT as JwtProvider

    Admin->>UI: Submit credentials
    UI->>API: POST /auth/login {username, password}
    
    API->>API: RateLimiter Check
    
    API->>DB: findByUsername()
    DB-->>API: User (Hash, Role)
    
    API->>Crypto: verify(password, hash)
    Crypto-->>API: valid = true
    
    API->>JWT: generateToken(User)
    JWT-->>API: Signed JWT String
    
    API-->>UI: 200 OK + {token, role}
    UI->>UI: Save to localStorage
    UI-->>Admin: Render Dashboard View
```
