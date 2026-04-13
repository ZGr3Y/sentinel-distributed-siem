# 🎓 Guida Magistrale per l'Esame: Sentinel Distributed SIEM
## Relazione Critica tra Teoria (Slide) e Implementazione Pratica

> **Obiettivo:** Fornire una preparazione di eccellenza (obiettivo 30) per l'esame orale, spiegando non solo *cosa* è stato fatto, ma la *logica ingegneristica* e i *trade-off* dietro ogni scelta architetturale.

---

## 🏗️ Introduzione: La Filosofia di Sentinel
Sentinel non è un semplice software, ma un **ecosistema distribuito** progettato per scalabilità e resilienza. La sfida principale è stata trasformare i pattern teorici del corso in una soluzione industriale capace di gestire flussi massivi di dati (Log Ingestion) garantendo sicurezza e integrità.

Il sistema è suddiviso in 4 moduli Maven che riflettono la separazione delle responsabilità:
1. `sentinel-agent`: (The Source) Simula il traffico e invia log.
2. `sentinel-core`: (The Brain) Elabora, analizza e rileva minacce asincronamente.
3. `sentinel-api`: (The Gateway) Espone i dati in modo sicuro al mondo esterno.
4. `sentinel-dashboard`: (The View) Interfaccia React per l'investigatore.

---

## 🔒 MODULO 1: Sicurezza e Controllo degli Accessi

### 1.1 Hashing con Scrypt (`L5_Auth-hash`)
**Teoria:** L'esame potrebbe partire chiedendo perché non usiamo MD5 o SHA-1 per le password.
**Risposta Sentinel:** In Sentinel usiamo **Scrypt**. A differenza di BCrypt, Scrypt è **"Memory-Hard"**. Questo significa che per calcolare l'hash non serve solo CPU, ma molta memoria RAM. 
*   **Perché?** Gli attaccanti moderni usano hardware dedicato (ASIC o GPU) che hanno migliaia di core CPU ma poca memoria per singolo core. Scrypt neutralizza questo vantaggio rendendo il "cracking" delle password economicamente insostenibile.
*   **Trade-off:** Paghiamo un tempo di login leggermente più lungo per l'utente, ma otteniamo una protezione di livello militare contro attacchi a dizionario massivi.

### 1.2 Token JWT vs Sessioni Stateful (`L5_Token`)
**Teoria:** Qual è la differenza tra scalabilità verticale e orizzontale in sicurezza?
**Risposta Sentinel:** Abbiamo optato per **JWT (JSON Web Token)** eliminando le sessioni tradizionali (Cookie/JSESSIONID).
*   **Perché?** In un sistema distribuito, se l'utente si logga sul Server A, il Server B deve sapere chi è senza dover interrogare un database centrale o sincronizzare la memoria. Il JWT contiene già i dati dell'utente firmati crittograficamente (HMAC-SHA256). Il server è **Stateless**: ogni richiesta contiene in sé l'autorità per essere eseguita.

### 1.3 Reference Monitor (`L2_ReferenceMonitor` & `L13_RequestPipeline`)
**Teoria:** Come garantire che nessuna richiesta "salti" i controlli di sicurezza?
**Risposta Sentinel:** Abbiamo implementato il concetto di **Reference Monitor** attraverso una **Pipeline di Filtri** (Spring Security Filter Chain).
*   **Implementazione:** Ogni singola chiamata HTTP deve passare obbligatoriamente per il `JwtAuthenticationFilter`. Questo filtro funge da "dogana": se il token è assente o manipolato, la richiesta viene abbattuta prima ancora di toccare la logica di business. Garantiamo così le proprietà di: *Complete Mediation* (ogni accesso è controllato) e *Tamper-proof* (il codice di sicurezza è isolato).

---

## 📨 MODULO 2: Messaggistica e Ingestione Asincrona

### 2.1 Event-Driven Architecture con RabbitMQ (`L16_Messaging`)
**Teoria:** Perché usare un Broker di messaggi invece di semplici chiamate REST?
**Risposta Sentinel:** Se l'Agent mandasse i log via REST direttamente al Core, un attacco DDoS farebbe cadere l'intera API. Usando **RabbitMQ (protocollo AMQP)**, abbiamo implementato un **Disaccoppiamento Temporale**.
*   **Il Vantaggio:** RabbitMQ funge da "cuscinetto" (Buffer). Se arrivano 10.000 log al secondo e il Core ne può elaborare solo 1.000, RabbitMQ li tiene al sicuro in coda. Il sistema non crasha, semplicemente rallenta (Backpressure).

### 2.2 Idempotent Receiver (`L11_IdempotentReceiver`)
**Teoria:** Cosa succede se la rete fallisce dopo che ho inviato un messaggio ma prima della conferma?
**Risposta Sentinel:** Usiamo il pattern **Idempotent Receiver**. L'Agent genera un **UUID univoco** per ogni evento. Il Core, prima di elaborare, controlla se quel ID esiste già nel Database (Hash check).
*   **Perché?** Questo garantisce la semantica **"Exactly-Once Processing"** anche se RabbitMQ invia il messaggio due volte (At-Least-Once Delivery). Senza questo pattern, i grafici del SIEM mostrerebbero il doppio dei dati reali in caso di glitch di rete.

### 2.3 Asincronia con CompletableFuture (`L11_JavaCompletable`)
**Teoria:** Come gestire compiti lenti senza bloccare l'intero sistema?
**Risposta Sentinel:** Il Core riceve il messaggio, lo scrive sul DB (veloce) e poi lancia l'analisi pesante (slow) tramite `CompletableFuture.runAsync()`.
*   **Trick per il 30:** Spiega che abbiamo disabilitato l'auto-ack di RabbitMQ. Inviamo l'`ack` manuale solo *dentro* la CompletableFuture. Se l'analisi fallisce, il messaggio torna in coda automaticamente.

---

## 🛡️ MODULO 3: Resilienza (Resilience4J)

### 3.1 Rate Limiting (`L15_DPresilience`)
**Teoria:** Come distinguiamo un utente normale da un attacco DoS?
**Risposta Sentinel:** Usiamo l'algoritmo **Sliding Window** di Resilience4j.
*   **Logica:** Monitoriamo il traffico per IP. Se un IP supera le 100 richieste al minuto, il sistema smette di elaborare i suoi log e genera un `ALERT_DOS`. Questo protegge il database da operazioni di scrittura sature.

### 3.2 Circuit Breaker (`L16_CircuitBreaker`)
**Teoria:** Cosa succede se il Database PostgreSQL diventa lentissimo o cade?
**Risposta Sentinel:** Senza un Circuit Breaker, tutti i thread del server resterebbero bloccati in eterno aspettando il DB, portando al crash totale dell'API. 
*   **Logica:** Il Circuit Breaker monitora i fallimenti. Se superano una soglia, il circuito si apre (**OPEN**) e le richieste falliscono istantaneamente senza nemmeno provare a toccare il DB moribondo. Dopo un tempo di attesa, prova a riaprire (**HALF-OPEN**) per vedere se il sistema è guarito.

---

## 🚀 MODULO 4: Ottimizzazioni per Sistemi Distribuiti

### 4.1 Remote Facade e DTO (`L5_RemoteFacadeDTO`)
**Teoria:** Perché non restituire direttamente le entità del Database al Frontend?
**Risposta Sentinel:** Per evitare il **"Chatty Traffic"** (traffico chiacchierone). Invece di far fare al frontend 5 chiamate (una per gli alert, una per lo stato DB, una per le statistiche), la **Remote Facade** raggruppa tutto in un unico oggetto **DTO (DashboardSummaryDTO)**. Una sola chiamata, tutti i dati necessari.

### 4.2 Serialized LOB & Session State (`L6_SessionStateSLOB`)
**Teoria:** Quando è meglio un database NoSQL rispetto a uno Relazionale?
**Risposta Sentinel:** Abbiamo usato un approccio ibrido. Le tabelle sono relazionali, ma i dati flessibili (come le bozze dei report) sono salvati in colonne **JSONB** (Serialized Large Objects).
*   **Vantaggio:** Gestiamo dati "schema-less" (che cambiano spesso) senza dover fare complicate migrazioni di database, ma mantenendo la sicurezza e la robustezza di PostgreSQL.

---

## 🧐 MODULO 5: Analisi dei Pattern NON Utilizzati (Per l'eccellenza)

*   **Pattern MEMENTO (`L10_Memento`):** 
    *   *Perché NON utilizzato?* Il Memento serve per fare "Undo/Redo" (catturare lo stato interno). Sentinel è un sistema di **Stream Processing**. Ogni log è immutabile. Non ha senso fare l'"undo" di un attacco rilevato o di un evento passato. Preferiamo la persistenza (Session State) alla gestione dello stato in memoria del Memento.
*   **Pattern LEADER AND FOLLOWERS (`L12_LeaderFollowers`):**
    *   *Perché NON utilizzato?* Questo pattern serve per l'elezione di un master in un cluster geograficamente distribuito per evitare conflitti di scrittura. Nel nostro setup, la coordinazione è delegata a **RabbitMQ**, che gestisce nativamente la distribuzione dei task ai vari consumatori (Core) in modo bilanciato. Non serve un'elezione custom se l'infrastruttura di messaging è già resiliente.
*   **Pattern REMOTE PROXY (`L4_RemoteProxy`):**
    *   *Perché NON utilizzato?* Il proxy remoto puro (stile Java RMI) crea un accoppiamento forte tra client e server. Noi abbiamo scelto **REST + JWT**, che è lo standard per i microservizi moderni perché è agnostico rispetto al linguaggio (potremmo riscrivere l'agent in Python e il server in Go e tutto continuerebbe a funzionare).

---

## 🎯 Conclusione per l'Orale
Sentinel Distributed SIEM dimostra come un'architettura **stratificata** (Layered) e **disaccoppiata** (By Messaging) possa risolvere i problemi tipici di sicurezza e performance. L'applicazione rigorosa dei pattern di **Idempotenza**, **Resilienza** e **Asincronia** rende il sistema pronto per un ambiente di produzione reale.
