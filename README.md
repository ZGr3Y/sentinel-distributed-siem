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

## 📊 Design Patterns
This project implements several architectural and messaging patterns, including:
- **Idempotent Receiver**: Ensuring events are processed once.
- **Remote Facade**: Aggregated dashboard endpoints.
- **Serialized LOB**: Optimizing database storage for analytics.
- **Circuit Breaker**: System resilience during failures.

Refer to [DESIGN_PATTERNS.md](file:///home/paolo/Downloads/IDSD/Progetto/DESIGN_PATTERNS.md) for more details.

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
