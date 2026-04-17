# Project Guidelines

## Architecture
- This is a Maven multi-module backend plus a separate React frontend.
- Backend modules and boundaries:
  - `sentinel-agent`: event/log producer (publishes to RabbitMQ).
  - `sentinel-core`: ingestion, idempotency, analytics, alert generation.
  - `sentinel-api`: REST and security layer over core/common.
  - `sentinel-common`: shared entities, DTOs, and utilities used by other backend modules.
- Keep shared contracts in `sentinel-common` and avoid duplicating DTO/entity types in feature modules.
- Follow the event-driven flow: Agent -> RabbitMQ -> Core -> PostgreSQL; API reads/writes persisted state.

## Build And Test
- Preferred backend commands (run from repo root):
  - `./mvnw clean install`
  - `./mvnw test`
  - `./mvnw -pl sentinel-core test`
  - `./mvnw -pl sentinel-api test`
- Local runtime options:
  - Full stack: `docker compose up -d`
  - Infra only for manual dev: `docker compose up -d postgres rabbitmq`
  - Manual backend run: `./mvnw -pl <module> spring-boot:run`
- Frontend commands (run from `sentinel-dashboard`):
  - `npm install`
  - `npm run dev`
  - `npm run lint`
  - `npm run build`

## Coding Conventions
- Prefer existing project patterns and naming over introducing new abstractions.
- Use Lombok consistently where already used in domain/DTO classes; do not mix incompatible styles inside the same model area.
- Keep API controllers thin; put business logic in services.
- When adding cross-module features, wire them through `sentinel-common` contracts first, then core/api behavior.

## Pitfalls And Guardrails
- Ensure Java 21 compatibility for Maven builds.
- Many backend flows require RabbitMQ and PostgreSQL; start infra before integration-oriented runs.
- Security-sensitive changes in `sentinel-api` should preserve JWT-based auth behavior unless explicitly requested.
- Avoid hardcoding secrets; use environment variables (for example `JWT_SECRET`).

## Docs To Link (Do Not Duplicate)
- System overview and run modes: `README.md`
- Contribution and commit rules: `CONTRIBUTING.md`
- Architecture rationale: `Software Architecture Document.md`
- Detailed design choices: `Software Design Document.md` and `Technical Design Document.md`
- Requirements baseline: `Specifica Requisiti Software.md`
- UML references: `docs/uml/*.md`
- Pattern mapping context: `MAPPATURA_DESIGN_PATTERNS.md`
