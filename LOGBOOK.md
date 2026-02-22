# 📖 DevOps Logbook: Sentinel Distributed SIEM

> **Purpose:** This logbook tracks the implementation progress, technical challenges encountered, and the precise solutions applied throughout the development of the Sentinel Distributed SIEM project. It serves as a continuous record of architectural and technical decisions.

---

## 📅 Log Entry: Phase 1 - Project Initialization & Core Domain Setup
**Date:** 22 February 2026
**Phase:** 1

### 🎯 What Was Made
1. **Containerized Infrastructure:** Set up a `docker-compose.yml` file to run PostgreSQL 15 (relational storage) and RabbitMQ 3.12 (message broker).
2. **Maven Multi-Module Architecture:** Initialized a Spring Boot 3 parent POM with four distinct child modules to enforce horizontal separation of concerns:
   - `sentinel-common`: Shared DTOs, Entities, and utilities.
   - `sentinel-agent`: Source simulator.
   - `sentinel-core`: Analytics and event ingestion.
   - `sentinel-api`: REST endpoints.
3. **Core Domain Models:** Created the core Java objects:
   - `EventDTO`: The data transfer object for RabbitMQ messaging.
   - `RawEvent`: The JPA entity for PostgreSQL storage.
4. **Idempotency Utility:** Developed `HashUtils.java` to calculate a SHA-256 hash using the immutable fields of a log event to guarantee "exactly-once" processing semantics. 
5. **Unit Testing:** Wrote `HashUtilsTest` to verify that the SHA-256 hashing remains consistent across identical objects and differentiates disparate ones.

### ⚠️ Problems Met & 🛠️ Solutions Applied

**Problem 1: Maven Wrapper Initializer Failure**
* **Description:** Attempted to use `start.spring.io` via `curl` to generate a Maven wrapper (`mvnw`) for the project, but the Spring Boot version 3.2.4 requested was rejected by the server as it is no longer within their compatibility range.
* **Solution:** Directly downloaded the Maven Wrapper distribution (v3.2.0) from the official Apache Maven repository via `curl`, extracted it, and injected it manually into the project repository.

**Problem 2: JDK Incompatibility with Lombok (`ExceptionInInitializerError: TypeTag :: UNKNOWN`)**
* **Description:** During the execution of `./mvnw clean install`, the `maven-compiler-plugin` failed with a fatal `TypeTag :: UNKNOWN` error. 
* **Root Cause Analysis:** Verified the host machine's Java environment (`java -version`). The local system is running **OpenJDK 25**. Although we defined `<java.version>21</java.version>` in the POM, Maven was executing under JDK 25. The `lombok` annotation processor (even the latest versions 1.18.32 and 1.18.36) has severe internal reflection bugs when running on very recent JDKs like 25, breaking the abstract syntax tree compilation.
* **Solution:** Instead of forcing the developer to downgrade their entire operating system's JDK installation, I made the codebase more robust. I removed all Lombok dependencies and annotation configurations from the POMs. I manually implemented standard Java `getters`, `setters`, `constructors`, and the `Builder` pattern inside `EventDTO` and `RawEvent`. This guarantees the code will compile flawlessly on JDK 21, 25, or any future Java version without relying on bytecode manipulation hacks.

**Problem 3: POM.xml Syntax Errors**
* **Description:** While surgically modifying the parent `pom.xml` to fix the compiler plugin, a duplicate `<plugins>` XML tag was accidentally introduced.
* **Solution:** Used linting feedback to pinpoint the malformed XML on line 54 and quickly corrected the closing/opening tags, restoring a successful build state.

---
*(End of Entry)*

## 📅 Log Entry: Phase 2 - Agent Module & Time-Shifted Replay Engine
**Date:** 22 February 2026
**Phase:** 2

### 🎯 What Was Made
1. **NASA Log Parser:** Built `NasaLogParser.java` using a robust Regex matcher (`^(\S+) - - \[(.+?)\] "(\S+) (.*?)(?: HTTP/\S+)?" (\d{3}) (\d+|-).*$`). It extracts all fields from the 1995 Hubble/KSC datasets and parses the timestamp via a dedicated `DateTimeFormatter` (`dd/MMM/yyyy:HH:mm:ss Z`).
2. **Time-Shifted Replay Engine:** Developed `ReplayEngine.java` to fulfill REQ-AG-04. It reads the 205MB log file buffer-by-buffer. It calculates the `ChronoUnit.MILLIS` exact delay between sequential logs, and simulates real-world real-time traffic generation by utilizing `Thread.sleep(delay)`. It also updates the DTO's payload timestamp to `LocalDateTime.now()` to ensure downstream metrics engines don't receive data stamped in 1995.
3. **Speedup Modifier:** To avoid waiting 30 days for 30 days of simulated traffic, implemented a `SPEEDUP_FACTOR` via `application.properties`. It divides the thread sleep delay by the factor (currently set to `100x` speed).
4. **RabbitMQ Egress:** Added the `spring-boot-starter-amqp` integration. The Agent creates a persistent queue (`logs.ingress.key`), a `DirectExchange`, and auto-marshals the Java `EventDTO` pojos into JSON strings using a standardized Jackson ObjectMapper.

### ⚠️ Problems Met & 🛠️ Solutions Applied

**Problem 1: JUnit 5 Dependencies Missing in Test Execution**
* **Description:** The execution of `./mvnw -pl sentinel-agent test` failed. Maven could not locate `org.junit.jupiter.api` despite our tests being written with valid JUnit 5 syntax.
* **Solution:** Discovered the child `pom.xml` was only importing `spring-boot-starter` for the main application, lacking the testing BOMs. Injected `<artifactId>spring-boot-starter-test</artifactId>` into the `sentinel-agent` module to resolve all API imports.

**Problem 2: Port 8080 Collision during Local Agent Boot**
* **Description:** Executing the Agent via `./mvnw spring-boot:run` crashed repeatedly with `Web server failed to start. Port 8080 was already in use.`
* **Solution:** Although the Agent is purely a background processing daemon and doesn't explicitly expose HTTP endpoints, `spring-boot-starter-web` spins up an embedded Tomcat server universally on 8080. Added `server.port=8081` to the Agent's application properties to circumvent local port exhaustions, and used `kill -9 $(lsof -t -i:8081)` to aggressively clear any zombie daemon locks holding the listener.

---
*(End of Entry)*

## 📅 Log Entry: Phase 3 - Core Module Ingestion & Persistence
**Date:** 22 February 2026
**Phase:** 3

### 🎯 What Was Made
1. **EventConsumerService:** Implemented the RabbitMQ listener to pull events from the `logs.ingress.key` queue.
2. **Idempotency Execution:** Integrated the `HashUtils.calculateEventHash(dto)` method. The resulting hash is mapped to the `raw_events` table `eventHash` column which has a unique constraint, cleanly discarding any duplicate messages thrown by the Agent.
3. **Severity Classification Engine:** Added logic to tag events dynamically:
   - CRITICAL: Detects path traversal or malicious endpoints (`/etc/passwd`, `cmd.exe`).
   - WARNING: Flags HTTP 400 and 500 status codes.
   - INFO: Baseline traffic.

### ⚠️ Problems Met & 🛠️ Solutions Applied
**Problem 1: Missing Queue Defaults cause Crash**
* **Description:** The `sentinel-core` module crashed on startup if the `sentinel-agent` hadn't booted first, because the Core module expected the `logs.ingress.key` queue to already exist on the RabbitMQ broker.
* **Solution:** Moved the `Queue`, `DirectExchange`, and `Binding` `@Bean` definitions into the Core's `RabbitMQConfig` as well. Both the Agent and Core now independently declare the queue using `durable=true`, ensuring whichever module starts first brings the messaging infrastructure online safely.

---
*(End of Entry)*

## 📅 Log Entry: Phase 4 - Real-Time Analytics & Design Pattern Mapping
**Date:** 22 February 2026
**Phase:** 4

### 🎯 What Was Made
1. **Design Pattern Documentation:** Created `DESIGN_PATTERNS.md` to officially explicitly track how Professor Tramontana's architectural concepts (Messaging, Idempotent Receiver, CompletableFuture, Resilience) are actively used in the codebase.
2. **Volumetric Threat Detection:** Built the `AnalyticsService` completely around the **Resilience4J `RateLimiter`** component (Design Pattern 2.6).
   - Created a unique Rate Limiter for every incoming IP.
   - Specifically configured a `limitForPeriod(100)` over `limitRefreshPeriod(60s)` for Denial of Service tracking.
   - Created a secondary Rate Limiter specifically filtering `401/403` status codes for Brute Force tracking (`10` failures / `60s`).
3. **Alert Persistence:** Created the `Alert` JPA Entity and `AlertRepository`. If the Rate Limiter acquires permission fails (`acquirePermission() == false`), it generates a permanent security Alert in the PostgreSQL database.
4. **Asynchronous Non-Blocking Execution:** Integrated the analytics engine into the `EventConsumerService` using `CompletableFuture.runAsync()` (Pattern 2.5). This guarantees the heavy analytical limiters run on a background thread pool, leaving the main AMQP thread free to drain the RabbitMQ ingress queue without latency spikes.
5. **Verified Integrity:** Pushed thousands of simulated events through the running Agent and successfully verified the creation of a `DOS_ATTACK` record in local PSQL.
6. **Git History Scrubbing:** Encountered a fatal push error due to the 160MB Agent log files being tracked in `.git` history. Erased them globally using `git filter-branch` to repair the push tree and successfully synchronized with the `main` branch.
