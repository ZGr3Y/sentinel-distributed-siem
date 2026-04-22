# Sentinel User Manual (IT + EN)

## 1. Scope and Audience / Scopo e destinatari

### IT
Questo manuale e pensato per utenti che vedono Sentinel per la prima volta. Include:
1. Avvio rapido del sistema.
2. Uso delle funzionalita principali.
3. Modifiche raccomandate per uso sicuro e best practice operative.
4. Verifiche post-setup e troubleshooting.

### EN
This manual is for first-time Sentinel users. It includes:
1. Fast system startup.
2. Core feature usage.
3. Recommended security and operations best-practice changes.
4. Post-setup verification and troubleshooting.

## 2. Prerequisites / Prerequisiti

### IT
Requisiti minimi:
1. Docker + Docker Compose.
2. Browser moderno.
3. Opzionale per sviluppo manuale: JDK 21+, Node.js 18+, npm.

### EN
Minimum requirements:
1. Docker + Docker Compose.
2. Modern browser.
3. Optional for manual development: JDK 21+, Node.js 18+, npm.

## 3. Quick Start (Recommended) / Avvio rapido (consigliato)

### 3.1 Start full stack / Avvia stack completo

```bash
docker compose up -d
```

### 3.2 Access points / Punti di accesso

1. Dashboard: `http://localhost`
2. API: `http://localhost:8083`
3. RabbitMQ UI: `http://localhost:15672`

### 3.3 Stop environment / Spegni ambiente

```bash
docker compose down
```

## 4. First Login and Roles / Primo login e ruoli

### IT
Credenziali di default in ambiente `dev`:
1. `admin / admin`
2. `analyst / analyst`

Questi utenti sono seedati da [sentinel-api/src/main/java/com/sentinel/api/config/DataSeeder.java](../sentinel-api/src/main/java/com/sentinel/api/config/DataSeeder.java) solo con profilo `dev`.

### EN
Default credentials in `dev` profile:
1. `admin / admin`
2. `analyst / analyst`

These users are seeded by [sentinel-api/src/main/java/com/sentinel/api/config/DataSeeder.java](../sentinel-api/src/main/java/com/sentinel/api/config/DataSeeder.java) only in `dev` profile.

### Role behavior / Comportamento ruoli

1. Authenticated users can access dashboard, investigation, drafts.
2. `ADMIN` role is required for `/api/reports/**` endpoints.

Reference: [sentinel-api/src/main/java/com/sentinel/api/security/SecurityConfig.java](../sentinel-api/src/main/java/com/sentinel/api/security/SecurityConfig.java).

## 5. Feature Usage Guide / Guida uso funzionalita

### 5.1 Dashboard page

<img width="1696" height="1377" alt="image" src="https://github.com/user-attachments/assets/e8a2766a-8773-46aa-bbeb-6464241d6552" />


### IT
Cosa fa:
1. Mostra KPI sintetici (`totalEvents`, `totalAlerts`, `dosAttacks`, `bruteForceAttacks`).
2. Mostra stato sistema e ultimi alert.
3. Aggiornamento periodico lato frontend.

### EN
What it does:
1. Shows summary KPIs (`totalEvents`, `totalAlerts`, `dosAttacks`, `bruteForceAttacks`).
2. Shows system health and latest alerts.
3. Uses periodic frontend refresh.

Reference:
1. [sentinel-dashboard/src/pages/Dashboard.tsx](../sentinel-dashboard/src/pages/Dashboard.tsx)
2. [sentinel-api/src/main/java/com/sentinel/api/controller/DashboardController.java](../sentinel-api/src/main/java/com/sentinel/api/controller/DashboardController.java)

### 5.2 Investigation page

<img width="1696" height="1377" alt="image" src="https://github.com/user-attachments/assets/21fd47ee-6220-430b-9400-c487a2565ff4" />


### IT
Cosa fa:
1. Accetta lista IP (uno per riga).
2. Invia batch unico a `/api/investigation/batch`.
3. Visualizza alert per IP e classificazioni.

### EN
What it does:
1. Accepts a list of IPs (one per line).
2. Sends a single batch request to `/api/investigation/batch`.
3. Displays per-IP alert history and classifications.

Reference:
1. [sentinel-dashboard/src/pages/Investigation.tsx](../sentinel-dashboard/src/pages/Investigation.tsx)
2. [sentinel-api/src/main/java/com/sentinel/api/controller/InvestigationController.java](../sentinel-api/src/main/java/com/sentinel/api/controller/InvestigationController.java)

### 5.3 Session Drafts page

### IT
Cosa fa:
1. Salva e recupera bozza analisi utente lato server.
2. Associa la bozza all utente autenticato.
3. Evita perdita del lavoro intermedio.

### EN
What it does:
1. Saves and loads user draft state server-side.
2. Associates draft state with authenticated user.
3. Prevents loss of work in progress.

Reference:
1. [sentinel-dashboard/src/pages/SessionDrafts.tsx](../sentinel-dashboard/src/pages/SessionDrafts.tsx)
2. [sentinel-api/src/main/java/com/sentinel/api/controller/DraftController.java](../sentinel-api/src/main/java/com/sentinel/api/controller/DraftController.java)

## 6. Mandatory Hardening Checklist / Checklist hardening obbligatoria

### IT
Prima di usare il sistema fuori da sviluppo locale, applica tutte queste modifiche.

### EN
Before using the system outside local development, apply all these changes.

| Change now | Why | Where |
|---|---|---|
| Change `JWT_SECRET` | Prevent token forgery and secret reuse | [.env.example](../.env.example), [docker-compose.yml](../docker-compose.yml), [sentinel-api/src/main/resources/application.properties](../sentinel-api/src/main/resources/application.properties) |
| Change DB password | Prevent default credential abuse | [.env.example](../.env.example), [docker-compose.yml](../docker-compose.yml) |
| Change RabbitMQ credentials | Prevent broker takeover | [.env.example](../.env.example), [docker-compose.yml](../docker-compose.yml) |
| Restrict CORS origins | Prevent cross-origin abuse | [.env.example](../.env.example), [docker-compose.yml](../docker-compose.yml), [sentinel-api/src/main/java/com/sentinel/api/security/SecurityConfig.java](../sentinel-api/src/main/java/com/sentinel/api/security/SecurityConfig.java) |
| Set `SPRING_PROFILES_ACTIVE=prod` | Disable dev-only behavior in production | [.env.example](../.env.example), [sentinel-api/src/main/resources/application.properties](../sentinel-api/src/main/resources/application.properties) |
| Replace seeded default users | Prevent trivial login attacks | [sentinel-api/src/main/java/com/sentinel/api/config/DataSeeder.java](../sentinel-api/src/main/java/com/sentinel/api/config/DataSeeder.java) |
| Put API behind HTTPS reverse proxy | Protect credentials and tokens in transit | [docker-compose.yml](../docker-compose.yml), [sentinel-dashboard/nginx.conf](../sentinel-dashboard/nginx.conf) |
| Reduce exposed ports | Minimize attack surface | [docker-compose.yml](../docker-compose.yml) |

### 6.1 Example secure values / Esempio valori sicuri

```bash
# Generate strong JWT secret (example)
openssl rand -base64 32

# Example CORS allow-list
CORS_ORIGINS=https://dashboard.example.com,https://soc.example.com
```

## 7. Configuration Guide / Guida configurazione

### 7.1 Environment variables

Main runtime variables are defined in:
1. [.env.example](../.env.example)
2. [docker-compose.yml](../docker-compose.yml)

Most relevant variables:
1. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
2. `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASS`
3. `JWT_SECRET`
4. `CORS_ORIGINS`
5. `SPRING_PROFILES_ACTIVE`

### 7.2 Frontend API endpoint

Frontend API base URL is defined in [sentinel-dashboard/src/services/api.ts](../sentinel-dashboard/src/services/api.ts) via `VITE_API_URL` fallback.

Recommendation:
1. Set `VITE_API_URL` explicitly per environment.
2. Do not rely on localhost fallback in production.

## 8. Verification After Setup / Verifica dopo setup

### 8.1 Tier 1 - Health checks

```bash
docker compose ps
curl -u user:password http://localhost:15672/api/overview
curl http://localhost:8083/auth/login
curl http://localhost
```

Expected:
1. All required containers are up.
2. RabbitMQ API answers.
3. API responds (even 400/401 is acceptable at this step).
4. Dashboard entry page is reachable.

### 8.2 Tier 2 - Auth flow

```bash
TOKEN=$(curl -s -X POST http://localhost:8083/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token')

curl -H "Authorization: Bearer $TOKEN" http://localhost:8083/api/dashboard/summary
curl http://localhost:8083/api/dashboard/summary
```

Expected:
1. Valid token is returned for correct credentials.
2. Protected endpoint works with token.
3. Protected endpoint returns unauthorized without token.

### 8.3 Tier 3 - Data flow

```bash
docker exec sentinel-rabbitmq rabbitmqctl list_queues name messages consumers
docker exec sentinel-postgres psql -U sentinel -d sentinel_db -c "SELECT COUNT(*) FROM raw_events;"
docker exec sentinel-postgres psql -U sentinel -d sentinel_db -c "SELECT alert_type, COUNT(*) FROM alerts GROUP BY alert_type;"
```

Expected:
1. Queue exists and has active consumer.
2. Raw events are persisted.
3. Alerts are generated over time.

## 9. Troubleshooting Matrix / Matrice troubleshooting

| Symptom | Probable cause | Action |
|---|---|---|
| Cannot login | wrong credentials or missing seeded users | verify profile and users in DB |
| Dashboard empty | no ingestion or no API token | check agent mode, queue, auth token |
| API 429 | rate limiter exceeded | reduce request burst, review client polling |
| API 401/403 | token invalid/expired or role mismatch | re-login, inspect role and security rules |
| No alerts detected | thresholds not crossed | check generator mode and detection thresholds |
| Frontend cannot reach API | CORS or base URL mismatch | verify `CORS_ORIGINS` and `VITE_API_URL` |

## 10. Manual Development Mode / Modalita sviluppo manuale

### IT
Usa questa modalita per sviluppo rapido e debug.

### EN
Use this mode for faster iterative development and debugging.

```bash
# Infra only
docker compose up -d postgres rabbitmq

# Backend in 3 terminals
./mvnw -pl sentinel-core spring-boot:run
./mvnw -pl sentinel-api spring-boot:run
./mvnw -pl sentinel-agent spring-boot:run -Dspring-boot.run.arguments="--sentinel.agent.mode=generate"

# Frontend
cd sentinel-dashboard
npm ci
npm run dev
```

## 11. Operational Best Practices / Best practice operative

1. Keep secrets out of source control.
2. Use per-environment `.env` values.
3. Rotate credentials regularly.
4. Use least-privilege roles.
5. Keep only required ports publicly exposed.
6. Validate logs and alerts daily.
7. Run backend and frontend test/lint checks before release.

## 12. Related references / Riferimenti correlati

1. [README.md](../README.md)
2. [Software Architecture Document.md](../Software%20Architecture%20Document.md)
3. [Software Design Document.md](../Software%20Design%20Document.md)
4. [Technical Design Document.md](../Technical%20Design%20Document.md)
5. [docs/SYSTEM_DEEP_DIVE_IT_EN.md](./SYSTEM_DEEP_DIVE_IT_EN.md)
6. [docs/uml/Deployment_UML.md](./uml/Deployment_UML.md)
7. [docs/uml/Sequence_UML.md](./uml/Sequence_UML.md)

## 13. Go-Live Readiness Mini Checklist / Mini checklist pre go-live

### IT
Puoi considerare l ambiente pronto quando tutte queste condizioni sono vere:
1. Nessuna credenziale di default attiva.
2. JWT secret unico e non condiviso.
3. CORS ristretto ai domini previsti.
4. Endpoint protetti verificati con test manuali.
5. Monitoraggio base e backup DB abilitati.

### EN
You can treat the environment as ready when all are true:
1. No default credentials remain active.
2. JWT secret is unique and private.
3. CORS is restricted to intended domains.
4. Protected endpoints are manually verified.
5. Basic monitoring and DB backup are enabled.
