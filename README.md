# 🛡️ Sentinel Distributed SIEM

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

Refer to [MAPPATURA_DESIGN_PATTERNS.md](MAPPATURA_DESIGN_PATTERNS.md) for more details.

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
