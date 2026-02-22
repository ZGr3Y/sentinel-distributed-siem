# **Software Architecture Document (SAD)**

**Progetto:** Sentinel Distributed SIEM **Versione:** 2.0 **Autore:** Paolo Maria Scarlata

## **1\. Rappresentazione Architetturale**

Sentinel adotta un'architettura **Event-Driven** (EDA) mediata da Broker, integrata con uno stile a **Microservizi** (per quanto riguarda la separazione delle responsabilità).

### **1.1 Diagramma dei Componenti (Logica)**

`graph LR`  
    `subgraph "Producer Layer"`  
        `Agent[Sentinel Agent]`   
    `end`  
      
    `subgraph "Messaging Layer"`  
        `MQ[RabbitMQ Broker]`  
    `end`  
      
    `subgraph "Processing Layer"`  
        `Core[Sentinel Core]`  
        `Engine[Analysis Engine]`  
        `API[REST API Controller]`  
    `end`  
      
    `subgraph "Storage Layer"`  
        `DB[(PostgreSQL)]`  
    `end`

    `Agent -->|AMQP Async| MQ`  
    `MQ -->|Push| Core`  
    `Core -->|Events| Engine`  
    `Engine -->|Persist| DB`  
    `API -->|Query| DB`

## **2\. Decisioni Architetturali (Design Rationale)**

### **2.1 Perché RabbitMQ (Messaging)?**

* **Problema:** L'Agent simula picchi di traffico (Burst) che potrebbero saturare il Database se scritti in sincrono.  
* **Soluzione:** RabbitMQ agisce da **Buffer (Leaky Bucket)**. Il Core consuma i messaggi a velocità costante ("Pre-fetch count"), proteggendo il DB dal sovraccarico.  
* **Pattern:** *Asynchronous Messaging* (Slide L16).

### **2.2 Perché Remote Facade?**

* **Problema:** La Dashboard necessita di dati eterogenei (stato sistema, ultimi allarmi, metriche traffico). Fare 3 chiamate REST separate aumenta latenza e complessità client.  
* **Soluzione:** Un unico Facade (DashboardController) aggrega i dati lato server (dove la latenza DB è bassa) e restituisce un DTO composito coarse-grained.  
* **Pattern:** *Remote Facade* (Slide L5).

### **2.3 Perché Serialized LOB?**

* **Problema:** I report storici richiedono join complesse su milioni di righe (raw\_events). Eseguire queste query "on-demand" è proibitivo.  
* **Soluzione:** Materializzare il report giornaliero come oggetto JSON (LOB) e salvarlo in una singola colonna. La lettura diventa una SELECT primaria per ID/Data, O(1).  
* **Pattern:** *Serialized LOB* (Slide L6).

### **2.4 Perché Session State su DB (e non in JWT)?**

* **Problema:** L'utente vuole salvare bozze di investigazione lunghe (filtri complessi, note). Mettere questi dati nel JWT ne aumenterebbe la dimensione, rallentando ogni richiesta HTTP (banda rete).  
* **Soluzione:** Il JWT contiene solo l'identità (sub). Lo stato applicativo (bozza) è salvato sul server (user\_sessions), recuperato tramite ID utente.  
* **Pattern:** *Session State* (Slide L6).

## **3\. Strategie di Resilienza**

Implementate secondo le linee guida *Resilience4j* (Slide L17).

1. **Circuit Breaker:** Applicato sul Repository dei Report Storici.  
   * *Stato Chiuso:* Query passano.  
   * *Stato Aperto:* Se errori \> 50%, ritorna subito fallback (lista vuota) senza stressare il DB.  
   * *Stato Half-Open:* Dopo 5s, tenta una query di prova.  
2. **Retry (Opzionale):** Applicato alla connessione iniziale RabbitMQ (se il broker non è ancora pronto all'avvio del Core).