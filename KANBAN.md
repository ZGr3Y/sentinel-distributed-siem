# 📋 Sentinel SIEM Kanban Board

## 📝 Backlog (Future Stories & Ideas)

**API Module & Patterns**
- [ ] REQ-API-01: Remote Facade (`/api/dashboard/summary`)
- [ ] REQ-API-02: Request Batch (`/api/investigation/batch`)
- [ ] REQ-API-03: Serialized LOB (Daily statistics aggregation job)
- [ ] REQ-API-04: Circuit Breaker (Protect `daily_reports` queries)

**Security**
- [ ] REQ-SEC-01: JWT Authentication for APIs
- [ ] REQ-SEC-02: Session State Server-Side (`/api/draft`)

## 🏃🏽‍♂️ To Do (Next Up)
- [ ] REQ-API-01: Remote Facade (`/api/dashboard/summary`)

## 🚧 In Progress

## ✅ Done
- [x] Initial requirements discussion
- [x] Set up project tracking and initial Kanban board
- [x] Set up the base Java project structure
- [x] Implement the core Event model and data structures
- [x] REQ-AG-01..06: Implement Agent module and time-shifted replay engine
- [x] REQ-CORE-01..03: Implement Core module (Ingestion, Idempotency, Severity)
- [x] REQ-AN-01..03: Core Analytics (Sliding Window, DOS, Brute Force)
- [x] Define architecture based on Professor's design patterns (`DESIGN_PATTERNS.md`)
