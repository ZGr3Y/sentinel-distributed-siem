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
`server.port=${SERVER_PORT:8083}`

`# --- DATASOURCE ---`  
`spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/sentinel_db}`  
`spring.datasource.username=${DB_USERNAME:sentinel}`  
`spring.datasource.password=${DB_PASSWORD:sentinel_password}`  
`spring.jpa.hibernate.ddl-auto=update`  
`spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect`

`# --- RABBITMQ ---`  
`spring.rabbitmq.host=${RABBITMQ_HOST:localhost}`  
`spring.rabbitmq.port=${RABBITMQ_PORT:5672}`  
`spring.rabbitmq.username=${RABBITMQ_USER:user}`  
`spring.rabbitmq.password=${RABBITMQ_PASS:password}`  
`# Serializzazione JSON automatica`  
`spring.rabbitmq.listener.simple.message-converter=jsonMessageConverter`

`# --- SENTINEL CUSTOM ---`  
`jwt.secret=${JWT_SECRET:MySecretKeyForSigningHmacSha256}`  
`sentinel.analysis.dos-threshold=100`  
`sentinel.analysis.window-seconds=60`  
`sentinel.queue.ingress=logs.ingress.key`

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

1. **Generazione:** L'Agent assegna proattivamente un `UUID v4` criptograficamente univoco a ogni log (`eventId`).
2. **Ricezione:** Messaggio JSON arriva dal Broker.  
3. **Estrazione ID:** Il backend estrae ciecamente il `dto.getEventId()` inviato dall'Agent come primary key naturale.  
4. **Check-Then-Act (Atomico su DB):**  
   `try {`  
       `repository.save(event); // event_hash ha vincolo UNIQUE su DB`  
   `} catch (DataIntegrityViolationException e) {`  
       `log.warn("Duplicato scartato: " + event.getId());`  
       `// Rimbalzo di rete bloccato silenziosamente`  
   `}`  
   *Questo approccio sfrutta il DB come "source of truth" per l'idempotenza (Slide L11\_IdempotentReceiver), impedendo ai reali duplicati di rete di raddoppiare le statistiche o generare doppi allarmi, ma consente a infiniti eventi di business "identici" di essere immessi se dotati di UUID distinti.*

### **3.3 Advanced Attack Simulation (Generative Mode)**

Algoritmo progettato per accertare il funzionamento dei Rate Limiter di Sentinel-Core (DoS e Brute Force).

A differenza dei primi prototipi con filtraggio basato su Hash, l'introduzione di un tracciamento basato su `UUIDv4` permette l'immissione di payload perfettamente identici ad alta velocità senza innescare barriere artificiali di idempotenza e riproducendo 1:1 una frazione d'attacco volumetrico reale.

`// Pseudo-codice`  
`for (int i = 0; i < 150; i++) {`
    `attackQueue.add(EventDTO.builder()`
        `.eventId(UUID.randomUUID().toString()) // Garanzia Anti-Idempotenza`
        `.timestamp(LocalDateTime.now())` 
        `.sourceIp(attackerIp)`
        `.method("GET")`
        `.endpoint(generateNormalEndpoint())`
        `.bytes(1500L)` // Payload costante e verosimile
    `.build());`
`}`