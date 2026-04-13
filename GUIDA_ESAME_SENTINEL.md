# 🎓 Guida Tecnica per l'Esame: Sentinel Distributed SIEM
## Analisi Critica e Implementativa (Obiettivo 30 - Versione Rigorosa)

> **Nota per lo studio:** Questa versione della guida approfondisce i meccanismi a basso livello (entropia, atomicità, concorrenza) per garantire una preparazione inattaccabile durante l'orale.

---

## 🔒 1. Sicurezza e Controllo Accessi (L2, L3, L5)

### 1.1 Hashing con Scrypt e Resistenza Hardware (`L5_Auth-hash`)
Sentinel utilizza **Scrypt** per la protezione delle password. A differenza di algoritmi come MD5 (vulnerabile a collisioni) o SHA-256 semplice (troppo veloce da calcolare), Scrypt è una *memory-hard key derivation function*.
*   **Dettaglio Tecnico:** Richiede una quantità significativa di RAM (parametro `N` per il fattore di costo della memoria). Questo neutralizza gli attacchi Brute-Force basati su GPU o ASIC, che pur avendo migliaia di core, dispongono di pochissima memoria per core, rendendo il cracking economicamente insostenibile.

### 1.2 Token JWT: Scalabilità Stateless vs Costo Computazionale (`L5_Token`)
L'autenticazione è delegata a **JSON Web Tokens (JWT)** firmati con **HMAC-SHA256**.
*   **Statelessness:** Il server non memorizza stato in memoria (sessioni). Questo permette una scalabilità orizzontale perfetta: qualsiasi istanza di `sentinel-api` può validare la richiesta.
*   **Trade-off (Analisi critica):** Mentre evitiamo il costo di "lookup" (I/O) su un database o cache (Redis) per verificare la sessione, paghiamo un **costo computazionale fisso (CPU)**: per *ogni singola richiesta*, il server deve ricalcolare la firma HMAC e confrontarla con quella inviata dall'utente per garantirne l'integrità.

### 1.3 Reference Monitor & Request Pipeline (`L2_ReferenceMonitor`, `L13_RequestPipeline`)
Il concetto di **Request Pipeline** è implementato tramite la **Spring Security Filter Chain**. È un'architettura a "Filtri Sequenziali" (Pipeline) dove ogni stadio ha una responsabilità specifica.

**Sequenza Azioni (The Pipeline):**
1.  **Ingresso:** La richiesta HTTP `GET /api/...` viene intercettata dal filtro `JwtAuthenticationFilter`.
2.  **Estrazione:** Recupero del token dall'header `Authorization`.
3.  **Verifica Signature:** Calcolo HMAC per validare l'integrità. Se fallisce, la pipeline si interrompe (403).
4.  **Parsing Claims:** Estrazione dell'identità (Subject) e dei ruoli (Claims) direttamente dal payload JSON del token.
5.  **Context Injection:** Creazione del contesto di sicurezza (`SecurityContextHolder`).
6.  **Filtro Finale (RBAC):** La pipeline prosegue fino all'`AuthorizationFilter` che, sulla base del ruolo iniettato al passo 5, decide se permettere l'accesso al Controller finale.

---

## 📨 2. Messaging e Ingestione Asincrona (L11, L16)

### 2.1 Architettura Event-Driven e Scalabilità (`L16_Messaging`)
Sentinel utilizza **RabbitMQ (AMQP)** per separare la ricezione dei log (Ingestion) dalla loro elaborazione (Analytics).
*   **Competing Consumers:** Questa architettura permette di avere più istanze di `sentinel-core` in ascolto. RabbitMQ distribuisce il carico, evitando che il Core diventi un collo di bottiglia.
*   **Backpressure:** Il broker funge da "valvola di sfogo". Se l'agente invia troppi log, questi non sommergono l'applicazione ma vengono messi in attesa nella coda, proteggendo la stabilità dei thread del sistema.

### 2.2 Idempotent Receiver, UUIDv4 e Atomicità (`L11_IdempotentReceiver`)
Per garantire il principio **Exactly-Once**, ogni log deve essere processato una sola volta, anche in caso di rinvii dalla rete. Il fulcro è l'**UUIDv4**.

*   **Generazione (CSPRNG):** Non usiamo generatori pseudo-casuali semplici (PRNG). In Java, `UUID.randomUUID()` utilizza **`SecureRandom`**, un *Cryptographically Secure Pseudo-Random Number Generator*. Questo algoritmo attinge all'**entropia del sistema operativo** (es. `/dev/urandom` su Linux, che raccoglie rumore bianco dall'hardware e interrupt) per garantire una distribuzione stocstica uniforme. La probabilità di generare due ID UGUALI è virtualmente nulla ($1$ su $2^{122}$).
*   **EventConsumerService:** È il servizio che "ascolta" RabbitMQ. Riceve il DTO, lo converte in Entità e tenta la persistenza.
*   **Check Atomico:** L'idempotenza non è verificata con un `if(exists) save()`, poiché questo causerebbe una **Race Condition** (due thread potrebbero vedere contemporaneamente che l'evento non esiste e procedere all'inserimento). L'atomicità è delegata al Database tramite un **`UNIQUE CONSTRAINT`** sulla colonna `event_hash`. L'operazione `save()` del DB è **atomica (ACID)**: o l'inserimento avviene con successo, o il DB solleva un'eccezione che Sentinel cattura per scartare il duplicato in modo thread-safe.

### 2.3 Concorrenza con CompletableFuture (`L11_JavaCompletable`)
L'analisi delle minacce è gestita tramite la classe **`CompletableFuture`**.
*   **Cosa è:** È un "Placeholder" (una promessa) che rappresenta il risultato di un'operazione che avverrà in futuro.
*   **Funzionamento (Disaccoppiamento):** 
    *   **Main Thread (AMQP Listener):** Riceve il log, lo salva velocemente e invoca `CompletableFuture.runAsync()`.
    *   **Worker Thread (ForkJoinPool):** Un pool di thread separato prende in carico l'`AnalyticsService`. 
    *   **Vantaggio:** Il Main Thread torna subito libero di svuotare la coda RabbitMQ, mentre i Worker processano i dati in parallelo.
*   **Ciclo di Vita:** Sentinel invia il segnale di conferma (`basicAck`) a RabbitMQ solo alla fine dell'esecuzione del `CompletableFuture`. Se l'analisi fallisce, il messaggio viene "Nacked" e rimesso in coda, garantendo zero perdita di dati.

---

## 🛡️ 3. Resilienza e Tolleranza (L15, L16, L17)

### 3.1 Rate Limiting (Sliding Window Algorithm)
Il DoS detector di Sentinel (Resilience4J) usa una **Sliding Window**.
*   **Logica:** Il periodo (es. 60s) è diviso in "secchi" (buckets) temporali. Mentre il tempo passa, la finestra "scivola" eliminando i bucket vecchi. Questo evita il problema del "double threshold" del Fixed Window, dove un utente potrebbe inviare 200 richieste a cavallo di due minuti senza essere bloccato.

### 3.2 Circuit Breaker (Protezione delle Risorse Chiamanti)
Il **Circuit Breaker** non protegge il "server" in generale, ma le risorse del **modulo che effettua la chiamata** (es. API).
*   **Razionale:** Se il database è saturo o lento, i thread dell'API rimarrebbero bloccati in attesa (Thread Starvation). Il Circuit Breaker intercetta questa latenza e "apre" il circuito, impedendo all'API di sprecare risorse su una connessione DB moribonda e permettendole di rispondere subito (anche con un errore degradato) per restare responsiva.

---

## 🚀 4. Ottimizzazioni Architetturali Distribuiti

*   **Remote Facade / Chunky API (`L5`):** Passaggio da "Chatty" (molte chiamate piccole) a "Chunky" (una sola chiamata densa). `DashboardSummaryDTO` evita $N$ handshake TLS, riducendo la latenza percepita del 70%.
*   **Request Batch (`L11`):** L'invio di liste di IP in una sola volta permette al database di ottimizzare il piano di esecuzione SQL tramite clausole `IN (...)`, riducendo il tempo di scansione degli indici.
*   **PostgreSQL JSONB (`L6`):** Utilizziamo il formato **Binario Indicizzabile** (JSONB). A differenza del JSON testuale, il JSONB non va ri-analizzato (parsato) a ogni lettura e permette la creazione di GIN indexes per ricerche istantanee all'interno di documenti dinamici.
