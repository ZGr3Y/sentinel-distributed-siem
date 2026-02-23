# 🛡️ Sentinel Distributed SIEM

Sentinel is a distributed Security Information and Event Management (SIEM) system designed for real-time log ingestion, analysis, and threat detection. It leverages a modular architecture to scale ingestion and analytics independently.

## 🏗️ Architecture

The project follows a modular Spring Boot architecture:

- **`sentinel-agent`**: Log source simulator. Parses historical logs (e.g., NASA datasets) and replays them in real-time.
- **`sentinel-core`**: The brain of the SIEM. Handles event ingestion from RabbitMQ, idempotency, and threat detection (DoS, Brute Force).
- **`sentinel-api`**: REST API layer providing dashboards, investigation endpoints, and statistical reports.
- **`sentinel-common`**: Shared domain entities, DTOs, and utilities.

## 🛠️ Tech Stack

- **Java 25** (Open JDK)
- **Spring Boot 3**
- **RabbitMQ** (Message Broker)
- **PostgreSQL** (Relational Database)
- **Resilience4J** (Rate Limiting & Circuit Breakers)
- **Docker & Docker Compose**

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- JDK 25 or higher
- Maven (or use `./mvnw`)

### Running the Infrastructure
Start PostgreSQL and RabbitMQ using Docker:
```bash
docker-compose up -d
```

### Building the Project
```bash
./mvnw clean install
```

### Starting the Modules
You can start the modules independently. For example, to start the Core engine:
```bash
./mvnw -pl sentinel-core spring-boot:run
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
