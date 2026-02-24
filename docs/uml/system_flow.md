# Sentinel SIEM — System Flow UML

## 1. End-to-End Data Flow (Activity Diagram)

```mermaid
flowchart TD
    subgraph AGENT["🛰️ Sentinel Agent"]
        A1["Read NASA Log File<br/>(line by line, O(1) memory)"]
        A2["Parse Line<br/>(NasaLogParser regex)"]
        A3{"Line valid?"}
        A4["Calculate Δt between<br/>consecutive timestamps"]
        A5{"Δt > 0?"}
        A6["Thread.sleep(Δt / speedupFactor)<br/>(Time-Shifted Replay)"]
        A7["Generate SHA-256 Hash<br/>(HashUtils)"]
        A8["Shift timestamp → now()"]
        A9["Publish to RabbitMQ<br/>(sentinel.direct / log.ingress)"]
    end

    subgraph RABBIT["🐰 RabbitMQ"]
        R1["Exchange: sentinel.direct"]
        R2["Queue: sentinel.queue.ingress<br/>(durable)"]
    end

    subgraph CORE["⚙️ Sentinel Core"]
        C1["@RabbitListener consumes event"]
        C2["Recalculate SHA-256 hash"]
        C3["Classify Severity"]
        C4{"Severity?"}
        C5["CRITICAL<br/>(path traversal/cmd injection)"]
        C6["WARNING<br/>(statusCode ≥ 400)"]
        C7["INFO<br/>(default)"]
        C8["Persist RawEvent to PostgreSQL"]
        C9{"Duplicate?<br/>(UNIQUE constraint)"}
        C10["⚡ CompletableFuture.runAsync()"]
        C11["AnalyticsService.analyzeEvent()"]
    end

    subgraph ANALYTICS["🔍 Real-Time Analytics"]
        AN1["Check DOS<br/>(RateLimiter: 100 req/60s per IP)"]
        AN2["Check Brute Force<br/>(RateLimiter: 10 failures/60s per IP)"]
        AN3["Check Pattern Match<br/>(CRITICAL severity)"]
        AN4["Generate Alert → PostgreSQL"]
    end

    subgraph API["🌐 Sentinel API"]
        subgraph SECURITY["Security Pipeline (L5)"]
            S1["RateLimiterFilter<br/>(Guava: 50 req/s)"]
            S2["JwtAuthenticationFilter<br/>(Reference Monitor)"]
            S3["SecurityConfig<br/>(RBAC rules)"]
        end
        API1["/api/dashboard/summary<br/>(Remote Facade DTO)"]
        API2["/api/investigation/batch<br/>(Request Batch)"]
        API3["/api/draft<br/>(Session State Server-Side)"]
        API4["/api/reports/daily<br/>(Serialized LOB + Circuit Breaker)"]
        API5["/auth/login<br/>(Authenticator + Scrypt)"]
    end

    subgraph DASHBOARD["💻 React Dashboard"]
        D1["Login Page<br/>(username/password form)"]
        D2["Dashboard Page<br/>(health + metrics + alerts)"]
        D3["Investigation Page<br/>(batch IP query)"]
        D4["Session Drafts Page<br/>(persist to server)"]
    end

    subgraph DB["🗄️ PostgreSQL"]
        DB1[("raw_events")]
        DB2[("alerts")]
        DB3[("daily_reports")]
        DB4[("draft_states")]
        DB5[("users")]
    end

    %% Agent Flow
    A1 --> A2 --> A3
    A3 -->|Yes| A4
    A3 -->|No, skip| A1
    A4 --> A5
    A5 -->|Yes| A6 --> A7
    A5 -->|No| A7
    A7 --> A8 --> A9

    %% RabbitMQ
    A9 --> R1 --> R2

    %% Core Flow
    R2 --> C1 --> C2 --> C3 --> C4
    C4 --> C5
    C4 --> C6
    C4 --> C7
    C5 & C6 & C7 --> C8 --> C9
    C9 -->|"No (new)"| C10 --> C11
    C9 -->|"Yes (duplicate)"| C1

    %% Analytics
    C11 --> AN1 & AN2 & AN3
    AN1 & AN2 & AN3 -->|Threshold exceeded| AN4

    %% Database writes
    C8 -.-> DB1
    AN4 -.-> DB2

    %% API Security Pipeline
    D1 -->|POST /auth/login| API5
    API5 -.-> DB5
    D2 -->|GET with JWT| S1 --> S2 --> S3
    S3 --> API1
    S3 --> API2
    S3 --> API3
    S3 --> API4

    %% API to DB
    API1 -.-> DB1 & DB2
    API2 -.-> DB2
    API3 -.-> DB4
    API4 -.-> DB3

    %% Style
    classDef agent fill:#1e3a5f,stroke:#3b82f6,color:#fff
    classDef rabbit fill:#ff6600,stroke:#ff8c00,color:#fff
    classDef core fill:#1a3a1a,stroke:#22c55e,color:#fff
    classDef analytics fill:#3b1a5f,stroke:#a855f7,color:#fff
    classDef api fill:#5f1a1a,stroke:#ef4444,color:#fff
    classDef dashboard fill:#1a4a5f,stroke:#06b6d4,color:#fff
    classDef db fill:#4a3a1a,stroke:#eab308,color:#fff
```

---

## 2. Authentication & Authorization Sequence (Login Flow)

```mermaid
sequenceDiagram
    actor User
    participant Dashboard as React Dashboard
    participant RL as RateLimiterFilter<br/>(Guava)
    participant Auth as AuthController
    participant Repo as UserRepository
    participant Scrypt as SCryptUtil
    participant JWT as JwtUtils<br/>(auth0 java-jwt)

    User->>Dashboard: Enter username + password
    Dashboard->>RL: POST /auth/login {username, password}

    alt Rate limit exceeded
        RL-->>Dashboard: 429 Too Many Requests
    end

    RL->>Auth: Forward request
    Auth->>Repo: findByUsername("admin")

    alt User not found
        Auth-->>Dashboard: 401 Invalid credentials
    end

    Repo-->>Auth: User (passwordHash, role)
    Auth->>Scrypt: SCryptUtil.check(password, passwordHash)

    alt Password mismatch
        Auth-->>Dashboard: 401 Invalid credentials
    end

    Scrypt-->>Auth: true ✓
    Auth->>JWT: generateToken(userId, "ADMIN")

    Note over JWT: JWT.create()<br/>.withIssuer("sentinel-siem")<br/>.withSubject(userId)<br/>.withClaim("role", "ADMIN")<br/>.withIssuedAt(now)<br/>.withExpiresAt(+24h)<br/>.sign(HMAC256)

    JWT-->>Auth: eyJhbGciOi...
    Auth-->>Dashboard: {token, userId, role: "ADMIN", type: "Bearer"}
    Dashboard->>Dashboard: Store JWT in localStorage
    Dashboard-->>User: Redirect to /dashboard
```

---

## 3. Event Ingestion & Idempotency Sequence

```mermaid
sequenceDiagram
    participant Agent as Sentinel Agent
    participant RMQ as RabbitMQ Queue
    participant Consumer as EventConsumerService
    participant Hash as HashUtils (SHA-256)
    participant DB as PostgreSQL (raw_events)
    participant Analytics as AnalyticsService
    participant AlertDB as PostgreSQL (alerts)

    Agent->>Agent: Parse log line → EventDTO
    Agent->>Agent: HashUtils.calculateEventHash(event)
    Agent->>Agent: event.setTimestamp(now())
    Agent->>RMQ: basicPublish(event)

    RMQ->>Consumer: @RabbitListener delivers EventDTO

    Consumer->>Hash: calculateEventHash(dto)
    Hash-->>Consumer: "a1b2c3d4..."

    Consumer->>Consumer: classifySeverity(dto)
    Note over Consumer: CRITICAL if path ∈ {.., /etc/passwd, cmd.exe}<br/>WARNING if status ≥ 400<br/>INFO otherwise

    Consumer->>DB: repository.save(rawEvent)

    alt UNIQUE constraint violated
        DB-->>Consumer: DataIntegrityViolationException
        Consumer->>Consumer: log.warn("Duplicate discarded")
        Note over Consumer: Pattern: Idempotent Receiver (L11)<br/>DB acts as MessageDB
    else New event
        DB-->>Consumer: Saved ✓
        Consumer->>Analytics: CompletableFuture.runAsync(analyze)

        par DOS Check
            Analytics->>Analytics: RateLimiter("dos-" + ip)<br/>100 req / 60s
        and Brute Force Check
            Analytics->>Analytics: RateLimiter("bf-" + ip)<br/>10 failures / 60s
        and Pattern Match
            Analytics->>Analytics: severity == CRITICAL?
        end

        alt Threshold exceeded
            Analytics->>AlertDB: save(new Alert(type, ip, description))
        end
    end
```

---

## 4. RBAC Access Control Flow

```mermaid
flowchart LR
    subgraph JWT_Claims["JWT Token Claims"]
        ISS["iss: sentinel-siem"]
        SUB["sub: userId"]
        ROLE["role: ADMIN | ANALYST"]
        EXP["exp: +24h"]
    end

    subgraph Filter["JwtAuthenticationFilter<br/>(Reference Monitor)"]
        F1["Extract Bearer token"]
        F2["JWT.require(HMAC256)<br/>.withIssuer(sentinel-siem)<br/>.verify(token)"]
        F3["Extract role claim"]
        F4["Create GrantedAuthority<br/>ROLE_ADMIN or ROLE_ANALYST"]
    end

    subgraph SecurityConfig["SecurityConfig<br/>(RBAC Rules)"]
        R1["/auth/login → permitAll()"]
        R2["/api/reports/** → hasRole ADMIN"]
        R3["/api/** → authenticated()"]
    end

    subgraph Endpoints["Protected Endpoints"]
        E1["Dashboard ✅ ADMIN+ANALYST"]
        E2["Investigation ✅ ADMIN+ANALYST"]
        E3["Drafts ✅ ADMIN+ANALYST"]
        E4["Reports ✅ ADMIN only"]
    end

    JWT_Claims --> Filter --> SecurityConfig --> Endpoints
```
