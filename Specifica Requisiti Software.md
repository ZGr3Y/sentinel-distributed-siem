# **Specifica dei Requisiti Software (SRS) \- Sentinel Distributed SIEM**

**Versione:** 2.0 (Dettagliata) **Data:** 15 Febbraio 2026 **Autore:** Paolo Maria Scarlata **Stato:** Approvato

## **1\. Introduzione**

### **1.1 Scopo del Documento**

Il presente documento definisce i requisiti funzionali e non funzionali per il sistema **Sentinel**, un'architettura distribuita per l'ingestione e l'analisi di eventi di sicurezza. I requisiti sono formulati per soddisfare le specifiche del corso "Ingegneria dei Sistemi Distribuiti", con particolare enfasi sui Design Pattern architetturali.

### **1.2 Glossario**

* **Ingestion:** Il processo di acquisizione, normalizzazione e invio dei log al sistema.  
* **Time-Shifted Replay:** Tecnica di simulazione che riproduce eventi storici rispettando i delta temporali originali, traslati nel tempo presente.  
* **EventID:** Hash univoco calcolato sul contenuto grezzo del log per garantire l'idempotenza.

## **2\. Requisiti Funzionali (Granulari)**

### **2.1 Modulo Agent (Source Log Simulator)**

* **REQ-AG-01 (Modalità Storica - Lettura Streaming):** Quando in modalità `replay`, l'Agent **deve** leggere set di dati storici (es. NASA KSC) riga per riga utilizzando uno Stream\<String\> Java, garantendo un'occupazione di memoria costante O(1).  
* **REQ-AG-02 (Parsing & Normalizzazione):** L'Agent **deve** parsare ogni riga ASCII ed estrarre i seguenti campi, gestendo errori di formattazione scartando righe malformate (Fail-Safe):  
  * sourceIp (String)  
  * timestamp (Originale)  
  * method (Verb HTTP)  
  * endpoint (Path risorsa)  
  * statusCode (Int)  
  * bytes (Long, default 0 se "-")  
* **REQ-AG-03 (Calcolo Delta Temporale):** Per ogni coppia di eventi consecutivi E\_n ed E\_{n+1}, l'Agent **deve** calcolare \\Delta t \= T(E\_{n+1}) \- T(E\_n).  
* **REQ-AG-04 (Attesa Attiva):** Se \\Delta t \> 0, l'Agent **deve** sospendere l'esecuzione per \\Delta t millisecondi prima di inviare E\_{n+1}.  
* **REQ-AG-05 (Generazione ID Idempotente):** L'Agent **deve** generare un identificatore crittograficamente robusto (UUIDv4) e assegnarlo al campo eventId del DTO, al fine di garantire la tracciabilità univoca transazionale.  
* **REQ-AG-06 (Invio Asincrono):** L'Agent **deve** pubblicare l'evento normalizzato (DTO JSON) sull'Exchange RabbitMQ sentinel.direct adottando la chiave di instradamento `logs.ingress.key`.
* **REQ-AG-07 (Modalità Attiva - Generazione Dinamica):** Quando in modalità `generate`, l'Agent **deve** abbandonare il file storico per simulare traffico background sintetico su cui iniettare programmaticamente ondate di attacchi volumetrici e payload (Pattern Match, Brute Force, DoS) eludendo in modo trasparente i normali filtri di idempotenza per testare le difese del Core.

### **2.2 Modulo Core (Ingestion & Processing)**

* **REQ-CORE-01 (Idempotenza Hash-Based):** All'arrivo di un messaggio, il Core **deve** verificare l'esistenza dell'eventId nel database (o cache L1).  
  * **Vincolo:** Se eventId esiste, il messaggio viene scartato e loggato come "Duplicate".  
* **REQ-CORE-02 (Classificazione Severity):** Il Core **deve** assegnare una severità all'evento:  
  * CRITICAL: Se endpoint matcha la Regex (?i)(\\.\\.|/etc/passwd|cmd\\.exe|/bin/sh).  
  * WARNING: Se statusCode \>= 400\.  
  * INFO: Altrimenti.  
* **REQ-CORE-03 (Persistenza Eventi):** Gli eventi validi **devono** essere salvati nella tabella raw\_events con timestamp di ingestione corrente (now()).

### **2.3 Modulo Core (Real-Time Analytics)**

* **REQ-AN-01 (Sliding Window Per-IP):** Il sistema **deve** mantenere in memoria una finestra mobile di 60 secondi per ogni IP sorgente attivo.  
* **REQ-AN-02 (Detection DOS):** Se il numero di richieste in finestra per un IP \> 100, il sistema **deve** generare un Alert di tipo DOS\_ATTACK.  
* **REQ-AN-03 (Detection Brute Force):** Se il numero di risposte con statusCode 401/403 in finestra per un IP \> 10, il sistema **deve** generare un Alert di tipo BRUTE\_FORCE.
* **REQ-AN-04 (Pattern Match Detection):** Se la gravità dell'evento in ingresso è classificata come CRITICAL, il sistema **deve** generare immediatamente un Alert di tipo PATTERN\_MATCH.

### **2.4 Modulo API & Pattern**

* **REQ-API-01 (Remote Facade):** L'endpoint GET /api/dashboard/summary **deve** restituire in un singolo oggetto JSON (DTO):  
  * Stato di salute (UP/DOWN) di DB e RabbitMQ.  
  * Conteggio totale eventi (ultimi 10 min).  
  * Lista ultimi 10 Alert.  
* **REQ-API-02 (Request Batch):** L'endpoint POST /api/investigation/batch **deve** accettare una lista di N indirizzi IP e restituire una lista di N report dettagliati in un'unica risposta HTTP.  
* **REQ-API-03 (Serialized LOB):** Un processo in background (job notturno) **deve** aggregare le metriche statistiche giornaliere, serializzarle in formato JSON e preservarle allocandole in un campo di tipo testuale stratificato (TEXT) in seno alla tabella `daily_reports`, limitando il carico computazionale in letture complesse.  
* **REQ-API-04 (Circuit Breaker):** Le query verso daily\_reports **devono** essere protette da Circuit Breaker. In caso di timeout DB (\> 2s), il sistema restituisce un report vuoto o cached, non un errore 500\.

### **2.5 Sicurezza**

* **REQ-SEC-01 (JWT Authentication):** Tutte le API (eccetto /auth/login) richiedono un token Bearer JWT valido (algoritmo HMAC-SHA256).  
* **REQ-SEC-02 (Session State Server-Side):** L'endpoint POST /api/draft **deve** salvare lo stato di lavoro dell'utente su DB, associandolo al userId estratto dal token, senza includere questi dati nel payload del token stesso.

## **3\. Requisiti Non Funzionali**

* **RNF-01 (Performance):** L'overhead di analisi per singolo evento deve essere \< 50ms.  
* **RNF-02 (Reliability):** Nessun evento deve essere perso in caso di crash del Core (garantito da code durevoli RabbitMQ).  
* **RNF-03 (Data Integrity):** Nessun evento duplicato deve essere processato (Idempotenza).