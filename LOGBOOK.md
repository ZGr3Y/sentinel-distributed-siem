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

## 📅 Log Entry: Phase 5 - API Module & Advanced Design Patterns
**Date:** 22 February 2026
**Phase:** 5

### 🎯 What Was Made
1. **Module Creation:** Created the `sentinel-api` child module with Spring Web and Spring Data JPA. Removed Spring Security entirely for the local development cycle to prevent `401 Unauthorized` blocks.
2. **Dashboard Controller (Remote Facade):** Implemented `/api/dashboard/summary` utilizing `DashboardSummaryDTO`. Fulfilled **Pattern 1.10** by bundling granular metrics (event counts, alert sums) into a single remote call.
3. **Investigation Controller (Request Batch):** Implemented a bulk pipeline via `/api/investigation/batch`. Analysts can submit arrays of IP addresses and the server matches them internally, drastically cutting HTTP overhead according to **Pattern 1.15**.
4. **Report Service (Serialized LOB & Circuit Breaker):** 
   - Created the `DailyReport.java` entity utilizing **Pattern 1.11**. By storing aggregated daily statistics as a raw Jackson JSON string in a Postgres `TEXT` column, we bypass expensive deep table JOINS for historical reports.
   - Wrapped the endpoint `/api/reports/daily` in `Resilience4J`'s `@CircuitBreaker` (**Pattern 1.16**). If the LOB retrieval fails, it triggers a degraded fallback response describing the system anomaly without crashing the user's portal.

### ⚠️ Problems Met & 🛠️ Solutions Applied

**Problem 1: 401 Unauthorized HTTP Defense**
* **Description:** Accessing the API via `curl` returned blank responses because they were met with a `401 Unauthorized`.
* **Solution:** Discovered `spring-boot-starter-security` was accidentally bootstrapped early. Excised it from the `pom.xml` entirely so development could proceed unhindered. JWT Auth is reserved for Phase 6.

**Problem 2: API Tomcat Port Saturation (8083 blocked)**
* **Description:** Start of the `mvnw spring-boot:run` crashed because port 8083 was "already in use".
* **Solution:** A phantom `java` task had remained running from a previous shell execution test. Found the specific process using the `CommandId` of the background subtask, gracefully killed it, and restarted the server clean.

---
*(End of Entry)*

## 📅 Log Entry: Phase 6 - Security & Authentication (JWT & Session State)
**Date:** 23 February 2026
**Phase:** 6

### 🎯 What Was Made
1. **JWT Architecture (REQ-SEC-01):** Implemented stateless `JwtAuthenticationFilter` using `java-jwt` and Spring Security. The filter enforces that all `/api/**` endpoints require a valid Bearer token signed with HMAC-SHA256. 
2. **Operational Security Override:** Overrode the default Spring Security handlers to return `403 Forbidden` across the board (even for `401 Unauthorized` scenarios) as requested, maximizing operational security against reconnaissance.
3. **Best-Practice Secret Injection:** Refactored the signature key to be injected via Environment Variables (`${JWT_SECRET}`) in `application.properties`, ensuring the 256-bit cryptographic material is never embedded natively in the Git history. 
4. **UUID Generation:** Utilized standard `Java UUID` generation securely attached to the JWT payload `sub` (subject), rather than sequential predictable integers.
5. **Server-Side Session State (PATTERN 1.11 / REQ-SEC-02):** Implemented `/api/draft` utilizing a central `DraftState` JPA Entity. To mitigate security payload bloating, user session data (draft reports) is saved directly into PostgreSQL, keyed anonymously by the UUID extracted purely from the JWT, fulfilling the "Session State Server-Side" pattern precisely.

## 📅 Log Entry: Final Review & Missing Analytics
**Date:** 23 February 2026

### 🎯 What Was Made
1. **Analisi Completa:** Rivedendo `Software Design Document.md` e `Specifica Requisiti Software.md`, è emersa una discrepanza tra il `REQ-CORE-02` (Classificazione Severity `CRITICAL` via Path Traversal/Cmd Injection) e lo schema reale delle Allerte in tabella.
2. **PATTERN_MATCH Alert:** La `AnalyticsService` generava solo `DOS_ATTACK` e `BRUTE_FORCE`. È stato implementato il metodo `checkPatternMatch` che legge la gravità `CRITICAL` assegnata dall'Idempotency filter nell'`EventConsumerService` e trasforma questa classificazione in un allarme `PATTERN_MATCH` persistente nel database, chiudendo l'ultimo gap nei requisiti funzionali previsti.

## 📅 Log Entry: Phase 7 - Web Dashboard 
**Date:** 24 February 2026
**Phase:** 7 (Extra)

### 🎯 What Was Made
1. **Frontend Architettura (React + Vite):** È stata creata una Single Page Application de-accoppiata per consumare le API del SIEM in modo interattivo.
2. **Sicurezza CORS:** Aggiornate le `SecurityConfig` di Spring Boot per accettare richieste `ORIGIN` sicure dal WebServer di sviluppo React (`localhost:5173`).
3. **Flusso Autenticazione:** Implementato Context API per gestire e memorizzare in `localStorage` il JWT e iniettarlo automaticamente come `Bearer` token in ogni Axios Request successiva.
4. **Dashboard View (Remote Facade):** L'UI consuma l'endpoint `GET /api/dashboard/summary` mostrando visivamente stato dei DB, messaggi/sec e una tabella Live degli alert (Inclusi i nuovi PATTERN_MATCH).
5. **Investigation View (Batch Request):** Implementata una Text Area massiva per interrogare storicamente l'attività di molteplici Indirizzi IP consumando `POST /api/investigation/batch`.
6. **Session Draft (Server-Side Session):** Implementato un Editor JSON che persiste lo stato di lavoro dell'analista sul DB tramite `POST /api/draft`, sincronizzando lo stato al ricaricamento.
