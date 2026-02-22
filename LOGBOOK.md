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
