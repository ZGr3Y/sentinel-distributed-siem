# **Software Design Document (SDD)**

**Progetto:** Sentinel Distributed SIEM **Versione:** 2.0

## **1\. Design del Database (Schema PostgreSQL)**

### **Tabella: raw\_events**

Memorizza il flusso di eventi in ingresso.

* id (**BIGSERIAL**, PK): Identificativo auto-incrementante.  
* event\_hash (**VARCHAR(64)**, UNIQUE): Hash SHA-256 per idempotenza. Indice B-Tree.  
* source\_ip (**VARCHAR(45)**): Supporta IPv4 e IPv6. Indice per ricerche veloci.  
* request\_path (**TEXT**): Path della richiesta (es. /index.html).  
* status\_code (**INTEGER**): Codice HTTP.  
* severity (**VARCHAR(10)**): Enum ('INFO', 'WARNING', 'CRITICAL').  
* ingested\_at (**TIMESTAMP**): Data inserimento (Default NOW()).

### **Tabella: alerts**

Memorizza le anomalie rilevate.

* id (**BIGSERIAL**, PK).  
* alert\_type (**VARCHAR(50)**): Enum ('DOS\_ATTACK', 'BRUTE\_FORCE', 'PATTERN\_MATCH').  
* source\_ip (**VARCHAR(45)**).  
* description (**TEXT**): Dettagli dell'anomalia (es. "Rate: 150 req/min").  
* created\_at (**TIMESTAMP**).

### **Tabella: daily\_reports (Pattern: Serialized LOB)**

* report\_date (**DATE**, PK): Chiave primaria naturale.  
* report\_json (**JSONB**): Oggetto JSON binario contenente il report aggregato.

## **2\. Design delle API (Interfacce REST)**

### **2.1 Dashboard Facade**

**GET** /api/v1/dashboard/summary

* **Response DTO (DashboardSummaryDTO):**  
  `{`  
    `"systemHealth": {`  
      `"database": "UP",`  
      `"broker": "UP"`  
    `},`  
    `"metrics": {`  
      `"totalEventsLast10Min": 1540,`  
      `"eventsPerSecond": 2.5`  
    `},`  
    `"latestAlerts": [`  
      `{ "id": 101, "type": "DOS_ATTACK", "ip": "192.168.1.5", "time": "..." }`  
    `]`  
  `}`

### **2.2 Batch Investigation (Pattern: Request Batch)**

**POST** /api/v1/investigation/batch

* **Request Body:**  
  `{`  
    `"ipAddresses": ["10.0.0.1", "192.168.1.50", "172.16.0.1"]`  
  `}`

* **Logica:** Il controller itera sulla lista (o usa query IN (...)) e costruisce una lista di DTO.  
* **Response Body:** List\<IpRiskReportDTO\>.

## **3\. Class Design (Componenti Core)**

### **3.1 EventConsumerService (Ingestion & Demultiplexing)**

Servizio Spring (@Service) responsabile del consumo e classificazione primordiale.

* **Metodo:** processMessage(EventDTO event)  
* **Annotazione:** @RabbitListener(queues \= "${sentinel.queue.ingress}")  
* **Flusso:**  
  1. Calcola Hash di event (Idempotency Filter).  
  2. if (rawEventRepository.existsByEventHash(hash)) return; (Scarta silente).  
  3. Mappa DTO \-\> Entity RawEvent.  
  4. Salva su DB.  
  5. Assegna una Severity basata su Regex e invoca `analyticsService.analyzeEvent(event)` delegando l'analisi pesante in thread asincrono separato.

### **3.2 AnalyticsService (Rate Limiting & Threat Detection)**

Componente Singleton (@Service).

* **Campo:** `RateLimiterRegistry rateLimiterRegistry` fornito da Resilience4j.
* **Metodi di Detection:** `checkDos(sourceIp)`, `checkBruteForce(event)`, `checkPatternMatch(event, sourceIp)`
* **Flusso (Esempio trino combinato):**
  1. Le chiamate Volume-Based ottengono il RateLimiter per l'IP via `rateLimiterRegistry.rateLimiter(...)`.
  2. Fallimento del Limit (Es. DoS > 100/min o BF > 10 fails/min) scatena subito i rispettivi alert volumetrici se fuori dal periodo di Cooldown per l'IP.
  3. Elaborazione Pattern Match (Payload HTTP infetti `../../etc/passwd`) aggira Resilience4j verificando direttamente la gravità `CRITICAL` ereditata ed emettendo flag istantanei sul database.
  4. L'ibridazione Resilience4j + Pattern statici garantisce concorrenza thread-safe e altissime prestazioni, superando le logiche di Database Polling tradizionali.