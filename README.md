# 🛡️ Sentinel Distributed SIEM

Sentinel is a distributed Security Information and Event Management (SIEM) system designed for real-time log ingestion, analysis, and threat detection. It leverages a modular architecture to scale ingestion and analytics independently.

## 🏗️ Architecture

The project follows a modular Spring Boot architecture:

- **`sentinel-agent`**: Log source simulator. Parses historical logs (e.g., NASA datasets) and replays them in real-time.
- **`sentinel-core`**: The brain of the SIEM. Handles event ingestion from RabbitMQ, idempotency, and threat detection (DoS, Brute Force).
- **`sentinel-api`**: REST API layer providing dashboards, investigation endpoints, and statistical reports.
- **`sentinel-common`**: Shared domain entities, DTOs, and utilities.

## 🛠️ Tech Stack

- **Java 21** (Open JDK)
- **Spring Boot 3**
- **Node.js & React** (Frontend Dashboard)
- **RabbitMQ** (Message Broker)
- **PostgreSQL** (Relational Database)
- **Resilience4J** (Rate Limiting & Circuit Breakers)
- **Docker & Docker Compose**

## 🚀 Local Deployment

To run the complete Sentinel SIEM platform locally (Infrastructure, Backend, and Frontend), follow these steps:

### Prerequisites
- Docker & Docker Compose
- JDK 21 or higher
- Maven (or use `./mvnw`)
- Node.js (v18+) & npm

### 1. Start the Infrastructure
Start the PostgreSQL database and RabbitMQ message broker:
```bash
docker-compose up -d
```

### 2. Build and Start Backend Services
Build the root project (this builds common, agent, core, and api modules):
```bash
./mvnw clean install -DskipTests
```
Open three separate terminals and start each backend module:

**Core Engine:** (Processes events and detects threats)
```bash
./mvnw -pl sentinel-core spring-boot:run
```

**API Layer:** (Serves data to the frontend)
```bash
./mvnw -pl sentinel-api spring-boot:run
```

**Agent Simulator:** (Generates and sends logs)
```bash
./mvnw -pl sentinel-agent spring-boot:run
```

### 3. Start the Frontend Dashboard
Open a new terminal, install dependencies, and start the React app:
```bash
cd sentinel-dashboard
npm install
npm run dev
```
The dashboard will be available at `http://localhost:5173`.

### Stopping the Environment
- Stop the spring-boot apps and frontend by pressing `CTRL+C` in their terminals.
- Stop the Docker containers:
```bash
docker-compose down
```

## 🎓 Academic Context (Professor View)

This project was developed as a university course assignment, with a primary focus on correctly applying enterprise software architecture and integration patterns taught in lectures (e.g., Prof. Tramontana's slides).

The system architecture prioritizes **pattern demonstrations over raw performance**. For example, while a production SIEM would use Redis for rate-limiting and Elasticsearch for log storage, this academic version uses Spring/Resilience4j in-memory structures and PostgreSQL to clearly showcase the implementation of specific design patterns without introducing excessive infrastructural complexity.

### 📊 Applied Design Patterns
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

For exact file locations and detailed explanations of how each pattern is coded, please review the dedicated **[DESIGN_PATTERNS.md](DESIGN_PATTERNS.md)** document.

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
