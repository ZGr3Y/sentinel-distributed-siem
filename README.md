#  Sentinel Distributed SIEM

<p align="center">
	<img src="https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
	<img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.x" />
	<img src="https://img.shields.io/badge/Spring_Security-6-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security 6" />
	<img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logoColor=white" alt="Spring Data JPA" />
	<img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
	<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" />
	<img src="https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker Compose" />
	<img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
	<img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white" alt="RabbitMQ" />
	<img src="https://img.shields.io/badge/Resilience4j-2F3134?style=for-the-badge&logoColor=white" alt="Resilience4j" />
	<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT" />
	<img src="https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB" alt="React" />
	<img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript" />
	<img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite" />
</p>

Sentinel is a distributed Security Information and Event Management (SIEM) system designed for real-time log ingestion, analysis, and threat detection. It leverages a modular architecture to scale ingestion and analytics independently.

##  Architecture

The project follows a modular Spring Boot architecture:

- **`sentinel-agent`**: Log source simulator. Features an **Advanced Attack Simulator** that dynamically injects structured anomalies (DoS, Brute Force, Malicious Payloads) over a steady stream of background traffic, alongside historical Time-Shifted Replay capabilities.
- **`sentinel-core`**: The brain of the SIEM. Handles event ingestion from RabbitMQ, idempotency, and threat detection (DoS, Brute Force, Regex Pattern Matching).
- **`sentinel-api`**: REST API layer providing dashboards, investigation endpoints, and statistical reports.
- **`sentinel-common`**: Shared domain entities, DTOs, and utilities.

##  Continuous Integration (CI/CD)
The project enforces strict code quality and compatibility checks via GitHub Actions:
- **Backend Pipeline**: Validates compilation on JDK 21 utilizing the native Maven Wrapper (`./mvnw`).
- **Frontend Pipeline**: Enforces strict Node 20 / React Linter compliance and Production bundle validation.
- Branch Protections mandate that all CI pipelines pass before PRs can be merged into `main` or `develop`.

##  Tech Stack

##  Technologies

### Backend

- **Java 21**
- **Spring Boot 3**
- **Spring Security 6**
- **Spring Data JPA**
- **Spring AMQP**
- **Resilience4j**
- **JWT authentication**
- **Maven**

### Infrastructure

##  Local Deployment

- **Docker**
- **Docker Compose**
- **PostgreSQL**
- **RabbitMQ**

### Frontend

- **React**
- **TypeScript**
- **Vite**
- **Nginx**

### Quality & Testing

- **JUnit 5**
- **Mockito**
- **MockMvc**
- **Spring Boot Test**
- **JaCoCo**

##  Techniques

- **Event-driven architecture**: Agent -> RabbitMQ -> Core -> PostgreSQL.
- **Idempotent receiver**: duplicate events are filtered before persistence.
- **DTO-based communication**: controllers exchange concise request/response objects.
- **Rate limiting**: protects the API from excessive requests.
- **Circuit breaker + fallback**: keeps reporting resilient under failures.
- **Asynchronous processing**: analysis work runs without blocking ingestion.
- **Batch query pattern**: investigations query multiple IPs in a single request.
- **JWT + RBAC**: authentication and role-based access control for the API.
- **Mock-based unit testing**: services are verified in isolation with Mockito.
- **Web slice testing**: controller endpoints are tested with MockMvc.
- **Coverage reporting**: JaCoCo measures executed code paths during test runs.

##  Local Deployment

### Prerequisites
- Docker & Docker Compose
- JDK 21 or higher (only for manual development mode)
- Maven (or use `./mvnw`) (only for manual development mode)
- Node.js (v20+) & npm (only for manual development mode)

### Option A: Full Stack via Docker (Recommended)
Start the entire system (infrastructure + all services) with a single command:
```bash
docker compose up -d
```
This builds and starts: PostgreSQL, RabbitMQ, sentinel-core, sentinel-api, sentinel-agent (generate mode), and the React dashboard.

- **Dashboard**: `http://localhost`
- **API**: `http://localhost:8083`
- **RabbitMQ Management**: `http://localhost:15672` (user/password)

To stop:
```bash
docker compose down
```

### Option B: Manual Development Mode
Use this when actively developing code — you get hot-reload on both backend and frontend.

**1. Start Infrastructure Only:**
```bash
docker compose up -d postgres rabbitmq
```

**2. Start Backend Services** (three separate terminals):
```bash
./mvnw -pl sentinel-core spring-boot:run
./mvnw -pl sentinel-api spring-boot:run
./mvnw -pl sentinel-agent spring-boot:run -Dspring-boot.run.arguments="--sentinel.agent.mode=generate"
```

**3. Start Frontend Dashboard:**
```bash
cd sentinel-dashboard
npm install
npm run dev
```
The dashboard will be available at `http://localhost:5173`.

<img width="1696" height="1377" alt="image" src="https://github.com/user-attachments/assets/2d545036-6d5e-4e68-b9b0-353da8946575" />


### Stopping the Environment
- Manual mode: press `CTRL+C` in each terminal, then `docker compose down`
- Docker mode: `docker compose down`

##  Academic Context (Professor View)

This project was developed as a university course assignment, with a primary focus on correctly applying enterprise software architecture and integration patterns taught in lectures (e.g., Prof. Tramontana's slides).

The system architecture prioritizes **pattern demonstrations over raw performance**. For example, while a production SIEM would use Redis for rate-limiting and Elasticsearch for log storage, this academic version uses Spring/Resilience4j in-memory structures and PostgreSQL to clearly showcase the implementation of specific design patterns without introducing excessive infrastructural complexity.

###  Applied Design Patterns
The following patterns have been explicitly implemented in the codebase:

1. **Messaging (1.8):** Asynchronous decoupling between Agent and Core via RabbitMQ.
2. **Idempotent Receiver (1.12):** Deterministic SHA-256 event hashing to prevent duplicate log ingestion.
3. **CompletableFuture (2.5):** Asynchronous threat analysis to unblock the ingestion queue.
4. **Rate Limiter (2.2 / 2.6):** Resilience4j dynamic sliding windows for DoS and Brute Force detection.
5. **Remote Facade & DTO (1.10):** Coarse-grained `DashboardSummaryDTO` to minimize frontend API calls.
6. **Request Batch (1.15):** Single `IN` clause database batching for multi-IP investigations.
7. **Circuit Breaker (1.16):** Fallback protection for heavy reporting queries.
8. **Authenticator & RBAC (1.2, 1.5):** JWT-based authentication with Spring Security Role-Based Access Control.
9. **Serialized LOB:** Storing complex, schema-less report snapshots inside a single database column.

For exact file locations and detailed explanations of how each pattern is coded, please review the dedicated **[MAPPATURA_DESIGN_PATTERNS.md](MAPPATURA_DESIGN_PATTERNS.md)** document.

##  Design Patterns
This project implements several architectural and messaging patterns, including:
- **Idempotent Receiver**: Ensuring events are processed once.
- **Remote Facade**: Aggregated dashboard endpoints.
- **Serialized LOB**: Optimizing database storage for analytics.
- **Circuit Breaker**: System resilience during failures.

##  License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
