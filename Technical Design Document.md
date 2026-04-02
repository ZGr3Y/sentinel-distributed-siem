# **Technical Design Document (TDD)**

**Progetto:** Sentinel Distributed SIEM **Versione:** 2.0

## **1\. Stack Tecnologico & Librerie (Slide Reference)**

| Componente | Libreria / Tool | Versione | Riferimento Slide |
| :---- | :---- | :---- | :---- |
| **JWT** | com.auth0:java-jwt | 4.4.0 | L5\_Token.pdf (Slide 9\) |
| **Resilience** | io.github.resilience4j:resilience4j-spring-boot3 | 2.1.0 | L17\_Resilience4J.pdf |
| **Messaging** | org.springframework.amqp:spring-rabbit | 3.1.0 | L16\_Messaging.pdf |
| **Database** | PostgreSQL JDBC Driver | 42.6.0 | \- |
| **Parsing** | Java Regex (java.util.regex) | JDK 21 | \- |

## **2\. Configurazioni Operative (Properties)**

### **application.properties (Backend)**

`# --- SERVER ---`  
`server.port=${SERVER_PORT:8080}`

`# --- DATASOURCE ---`  
`spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/sentinel_db}`  
`spring.datasource.username=${DB_USER:sentinel}`  
`spring.datasource.password=${DB_PASSWORD:sentinel_password}`  
`spring.jpa.hibernate.ddl-auto=update`  
`spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect`

`# --- RABBITMQ ---`  
`spring.rabbitmq.host=${RABBITMQ_HOST:localhost}`  
`spring.rabbitmq.port=${RABBITMQ_PORT:5672}`  
`spring.rabbitmq.username=${RABBITMQ_USER:user}`  
`spring.rabbitmq.password=${RABBITMQ_PASSWORD:password}`  
`# Serializzazione JSON automatica`  
`spring.rabbitmq.listener.simple.message-converter=jsonMessageConverter`

`# --- SENTINEL CUSTOM ---`  
`sentinel.jwt.secret=${JWT_SECRET:MySecretKeyForSigningHmacSha256}`  
`sentinel.analysis.dos-threshold=100`  
`sentinel.analysis.window-seconds=60`  
`sentinel.queue.ingress=sentinel.queue.ingress`

## **3\. Specifica Algoritmi**

### **3.1 Time-Shifted Replay (Agent)**

Algoritmo per garantire la fedeltà temporale della simulazione.  
`// Pseudo-codice`  
`LocalDateTime lastLogTime = null;`

`while ((line = reader.readLine()) != null) {`  
    `LogEntry entry = parse(line);`  
    `LocalDateTime currentLogTime = entry.getTimestamp();`

    `if (lastLogTime != null) {`  
        `// Calcola delta in ms`  
        `long deltaMs = ChronoUnit.MILLIS.between(lastLogTime, currentLogTime);`  
          
        `// Gestione Edge Case: Delta negativo (log disordinati) o zero`  
        `if (deltaMs > 0) {`  
            `// Applica fattore accelerazione (opzionale)`  
            `long sleepTime = deltaMs / SPEEDUP_FACTOR;`  
            `Thread.sleep(sleepTime);`   
        `}`  
    `}`  
      
    `// Aggiorna e Invia`  
    `lastLogTime = currentLogTime;`  
    `entry.setTimestamp(LocalDateTime.now()); // Shift al presente`  
    `rabbitTemplate.convertAndSend(entry);`  
`}`

### **3.2 Idempotency Check (Core)**

Strategia per garantire *Exactly-Once Processing* (simulato).

1. **Ricezione:** Messaggio JSON arriva dal Broker.  
2. **Hashing:** Il backend ricalcola l'hash SHA-256 sui campi immutabili (Timestamp Originale, IP, Method, Path, Bytes).  
   * *Nota:* Non usiamo l'ID generato dall'Agent ciecamente, ricalcolarlo è più sicuro, ma per semplicità accademica ci fidiamo del campo eventId nel DTO se presente.  
3. **Check-Then-Act (Atomico su DB):**  
   `try {`  
       `repository.save(event); // event_hash ha vincolo UNIQUE su DB`  
   `} catch (DataIntegrityViolationException e) {`  
       `log.warn("Duplicato scartato: " + event.getId());`  
       `// ACK manuale positivo per rimuoverlo dalla coda`  
   `}`  
   *Questo approccio sfrutta il DB come "source of truth" per l'idempotenza (Slide L11\_IdempotentReceiver, Soluzione "MessageDB").*

### **3.3 Advanced Attack Simulation (Generative Mode)**

Algoritmo progettato per accertare il funzionamento dei Rate Limiter di Sentinel-Core (DoS e Brute Force), mitigando l'ostacolo intrinseco imposto dal Controllo di Idempotenza (3.2).

Essendo le raffiche DoS composte da richieste identiche iniettate in cicli `while` sub-millisecondo, l'Agent genererebbe fisiologicamente Payload identici provocando il rigetto per Idempotenza prima ancora che la minaccia arrivasse agli Analizzatori logici. 

`// Pseudo-codice`  
`for (int i = 0; i < 150; i++) {`
    `attackQueue.add(EventDTO.builder()`
        `.timestamp(LocalDateTime.now())` // I timestamp possono coincidere sotto rapida iterazione
        `.sourceIp(attackerIp)`
        `.method("GET")`
        `.endpoint(generateNormalEndpoint())`
        `// EVASIONE IDEMPOTENZA:` 
        `// L'iniezione incrementale dell'iteratore nei Byte garantisce l'unicità matematica dell'Hash SHA-256`
        `.bytes(500L + i)`
    `.build());`
`}`