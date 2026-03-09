# Sentinel SIEM - Design Pattern Implementations

This document tracks how Professor Tramontana's design patterns from the lecture slides are systematically applied to the Sentinel SIEM project. 

## Implemented Patterns

### 1. Messaging (1.8)
*   **Slide Source:** `L16_Messaging.txt`
*   **Where it's used:** Between the `sentinel-agent` and `sentinel-core` modules.
*   **How it works:** We use RabbitMQ to create an asynchronous buffer. The Agent acts as a Producer sending security events. The Core acts as an **Event-Driven Consumer** (`EventConsumerService.java`), reacting to incoming messages rather than pulling on a schedule.

### 2. Idempotent Receiver (1.12)
*   **Slide Source:** Slide/Knowledge Base mapping.
*   **Where it's used:** Core Module (`EventConsumerService.java`).
*   **How it works:** To prevent the database from recording duplicate events if the network fails and retries, we calculate a deterministic SHA-256 hash (`HashUtils.java`) of the incoming event's attributes (IP, timestamp, endpoint). If a message arrives with a hash that already exists in the Postgres `raw_events` table, the unique constraint safely discards it.

### 3. CompletableFuture (2.5)
*   **Slide Source:** Knowledge Base (Async execution).
*   **Where it's used:** Core Module (`EventConsumerService.java` -> `AnalyticsService.java`).
*   **How it works:** When a fast influx of events arrives (like a DDoS), the ingestion thread needs to perform heavy analytics (checking for Brute Force/DOS rules). To avoid blocking the RabbitMQ ingestion loop, the analysis step is wrapped in a `CompletableFuture.runAsync()`, allowing it to run concurrently in a background thread pool.

### 4. Rate Limiter (2.2 / 2.6)
*   **Slide Source:** Knowledge Base (Gateway Protection / Resilience).
*   **Where it's used:** Core Module (`AnalyticsService.java`).
*   **How it works:** We use `Resilience4j`'s RateLimiter to dynamically track request volume per IP address. If an IP exceeds 100 requests per minute (DOS) or 10 failed authentications per minute (Brute Force), the RateLimiter triggers an internal alarm that generates an `Alert` entity in the database.

### 5. Remote Facade & Data Transfer Object (1.10)
*   **Where it's used:** API Module (`DashboardController.java` & `DashboardSummaryDTO.java`).
*   **How it works:** Instead of the frontend making numerous small requests to fetch alerts, logs, and stats individually, the `DashboardController` acts as a Remote Facade. It aggregates the data and returns a coarse-grained `DashboardSummaryDTO`, minimizing network trips and reducing chatty client-server behavior.

### 6. Request Batch (1.15)
*   **Where it's used:** API Module (`InvestigationService.java`).
*   **How it works:** When investigating multiple IPs simultaneously, the frontend sends a single `BatchQueryRequest` containing a list of IPs. The backend processes this using a single IN-clause database query, completely avoiding the N+1 query problem and returning all results as a `BatchQueryResponse`.

### 7. Circuit Breaker (1.16)
*   **Where it's used:** API Module (`ReportService.java`).
*   **How it works:** Using `Resilience4j`, the `getDailyReport()` method is protected by a Circuit Breaker. If the database is overwhelmed or unresponsive while generating the heavy daily report, the circuit opens and immediately returns a cached or fallback response, rather than causing cascading failures across the system.

### 8. Authenticator & Role-Based Access Control (1.2, 1.5)
*   **Where it's used:** Security Module (`SecurityConfig.java`, `AuthController.java`, `JwtTokenProvider.java`).
*   **How it works:** The system implements a robust Authenticator using JSON Web Tokens (JWT) and Scrypt password hashing. Once authenticated, Role-Based Access Control (RBAC) is enforced using Spring Security's `@PreAuthorize("hasRole('ADMIN')")` on sensitive API endpoints, ensuring students vs. professors have appropriate access boundaries.

### 9. Serialized LOB (Large Object) Pattern
*   **Where it's used:** Common/API Modules (`DailyReport.java`, `DraftState.java`).
*   **How it works:** Complex, heavily nested JSON configurations (like dashboard layout drafts) or generated report data are stored in the database as serialized JSON strings within a single column, rather than creating an overly complex and highly joined relational schema.
