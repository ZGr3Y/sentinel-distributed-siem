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

---

## Planned Patterns (Backlog)

*   **Remote Facade & Data Transfer Object (1.10):** To be implemented in the API module for fetching dashboard summaries.
*   **Request Batch (1.15):** To be implemented in the API module for bulk log retrieval.
*   **Circuit Breaker (1.16):** To be implemented in the API module to protect database queries.
*   **Role-Based Access Control & Authenticator (1.2, 1.5):** To be implemented in the Security module.
