# **Software Design Document (SDD)**

**Progetto:** Sentinel Distributed SIEM **Versione:** 2.0

## **1\. Design del Database (Schema PostgreSQL)**

### **Tabella: raw\_events**

Memorizza il flusso di eventi in ingresso.

* id (**VARCHAR(36)**, PK): Identificativo univoco universale (UUIDv4) assegnato alla sorgente.  
* event\_hash (**VARCHAR(64)**, UNIQUE): Identificatore crittografico primario ai fini dell'idempotenza transazionale (valorizzato con UUIDv4). Indice B-Tree.  
* source\_ip (**VARCHAR(45)**): Supporta IPv4 e IPv6. Indice per l'ottimizzazione delle ricerche.  
* request\_path (**TEXT**): Percorso formale della richiesta (es. /index.html).  
* status\_code (**INTEGER**): Codice di stato del protocollo HTTP.  
* severity (**VARCHAR(10)**): Enumerazione logica ('INFO', 'WARNING', 'CRITICAL').  
* ingested\_at (**TIMESTAMP**): Marca temporale di inserimento (Default NOW()).

### **Tabella: alerts**

Memorizza le anomalie rilevate.

* id (**VARCHAR(36)**, PK).  
* alert\_type (**VARCHAR(50)**): Enum ('DOS\_ATTACK', 'BRUTE\_FORCE', 'PATTERN\_MATCH').  
* source\_ip (**VARCHAR(45)**).  
* description (**TEXT**): Dettagli dell'anomalia (es. "Rate: 150 req/min").  
* created\_at (**TIMESTAMP**).

### **Tabella: daily\_reports (Pattern: Serialized LOB)**

* id (**VARCHAR(36)**, PK): Identificativo sintetico primario.
* report\_date (**DATE**, UNIQUE): Chiave logica naturale.  
* report\_data (**TEXT**): Struttura dati serializzata in formato JSON delineante le metriche aggregate.

## **2\. Design delle API (Interfacce REST)**

### **2.1 Dashboard Facade**

**GET** /api/dashboard/summary

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

**POST** /api/investigation/batch

* **Request Body:**  
  `{`  
    `"ipsToInvestigate": ["10.0.0.1", "192.168.1.50", "172.16.0.1"]`  
  `}`

* **Logica:** Il controller itera sulla lista (o usa query IN (...)) e costruisce una lista di DTO.  
* **Response Body:** List\<IpRiskReportDTO\>.

## **3\. Class Design (Componenti Core)**

### **3.1 EventConsumerService (Ingestion & Demultiplexing)**

Servizio Spring (@Service) responsabile del consumo e classificazione primordiale.

* **Metodo:** processMessage(EventDTO event)  
* **Annotazione:** @RabbitListener(queues \= "${sentinel.queue.ingress}")  
* **Flusso:**  
  1. Estrae o computa l'ID univoco dell'evento (UUIDv4) per l'Idempotency Filter.  
  2. Tenta la persistenza su database tramite `repository.save(event)`. In caso di duplicato, il vincolo UNIQUE su DB solleva una `DataIntegrityViolationException` gestita scartando silenziosamente il messaggio.  
  3. Mappa DTO \-\> Entity RawEvent prima del salvataggio.  
  4. Salva su DB.  
  5. Assegna una Severity basata su Regex e rigira a `analyticsService.analyzeEvent(event)` delegando l'analisi pesante in thread asincrono separato.

### **3.2 AnalyticsService (Rate Limiting & Threat Detection)**

Componente Singleton (@Service).

* **Campo:** `RateLimiterRegistry rateLimiterRegistry` fornito da Resilience4j.
* **Metodi di Detection:** `checkDos(sourceIp)`, `checkBruteForce(event)`, `checkPatternMatch(event, sourceIp)`
* **Flusso (Esempio trino combinato):**
  1. Le chiamate Volume-Based ottengono il RateLimiter per l'IP via `rateLimiterRegistry.rateLimiter(...)`.
  2. Fallimento del Limit (Es. DoS > 100/min o BF > 10 fails/min) scatena subito i rispettivi alert volumetrici se fuori dal periodo di Cooldown per l'IP.
  3. Elaborazione Pattern Match (Payload HTTP infetti `../../etc/passwd`) aggira Resilience4j verificando direttamente la gravità `CRITICAL` ereditata ed emettendo flag istantanei sul database.
  4. L'ibridazione Resilience4j + Pattern statici garantisce concorrenza thread-safe e altissime prestazioni, superando le logiche di Database Polling tradizionali.