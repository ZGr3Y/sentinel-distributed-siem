# Sentinel Distributed SIEM - Knowledge Base

This document consolidates the architectural design patterns, technologies, and features extracted from the course lecture slides to serve as a comprehensive reference for the implementation of the project.

## 1. Design Patterns

### 1.1 Proxy & Protection Proxy
- **Intent**: Provide a surrogate or placeholder for another object to control access to it.
- **Use Cases**: Lazy initialization (creation of expensive objects on-demand), remote access masking, and most importantly, access control.
- **Protection Proxy**: Implements authorization rules. It acts as a middleman between the client and the `RealSubject` (e.g., a `Book` class), verifying permissions (like reading or appending) by consulting an `AuthrzRules` component before forwarding the request.
- **Other Proxies**: *Cache Proxy* (temporarily holds results), *Copy-on-Write Proxy* (defers expensive copy operations until a modification is made). Multiple proxies can be chained (e.g., Protection + Cache).

### 1.2 Role-Based Access Control (RBAC) [Schumacher]
- **Intent**: Define access rights based on users' roles and organizational tasks, enforcing the "need-to-know" principle.
- **Structure**:
  - A `User` initiates a `Session`.
  - The `Session` maps the user to one or more active `Role`s.
  - A `Role` has a set of `Permission`s granting access to specific `ProtectionObject`s.
- **Benefits**: Simplifies security administration since roles change less frequently than users. The application checks permissions by asking `Session.hasPermission(p)`.

### 1.3 Remote Proxy
- **Intent**: Provide a local representative for an object that resides in a different address space (remote host).
- **Process**: The client accesses the remote service transparently using a `LocalBook` proxy. The proxy uses a `SockCommunicator` to send messages over the network to a `SockListener` on the server, which forwards them to a `ServerProxy` and finally to the `Book`.

### 1.4 Forwarder-Receiver
- **Intent**: Provide transparent inter-process communication for peer-to-peer interactions, decoupling peers from underlying communication mechanisms (like TCP/IP or sockets).
- **Structure**: 
  - **Peers**: Send and receive messages.
  - **Forwarder**: Maps names to physical addresses and sends messages over a specific IPC mechanism.
  - **Receiver**: Listens for messages and passes them to its peer.
- **Benefits**: Makes it easy to change the IPC mechanism without affecting the peer logic.

### 1.5 Authenticator
- **Intent**: Provide a single point of access to verify that the entity requesting access is legitimate.
- **Process**: Subject provides `AuthenticationInfo` (credentials). If verified, the Authenticator generates a `ProofOfIdentity` (Token/Credential) preventing the need for continuous logins.

### 1.6 Memento [Gamma]
- **Intent**: Capture and externalize an object's internal state without violating encapsulation, allowing the object to restore that state later.
- **Structure**:
  - `Originator`: Creates a `Memento` capturing its internal state. It takes the memento to restore state (undo/rollback).
  - `Memento`: The state container. It does not permit inspection of data by unauthorized entities.
  - `Caretaker`: Holds onto the `Memento` safely without reading it.

### 1.6 Request Batch
- **Intent**: Combine multiple small requests into a single batch request to dramatically reduce network latency, serialization overhead, and to improve throughput.
- **Process**: 
  - `RequestSender`: Accumulates queries for a specific timeframe or quantity limit.
  - `BatchAssembler`: Bundles them into one envelope for transmission.
  - `BatchProcessor`: On the server, unbundles and processes each request.
  - `ResponseSplitter`: Restores individual responses and maps them back to the original client requests utilizing request IDs.

### 1.7 Aspect-Oriented Programming (AOP)
- **Problem Solved**: Eliminates *crosscutting concerns* (e.g., logging, security, metrics) leading to *tangling/scattered* code throughout business logic.
- **Core Concepts**:
  - **Join Point**: A point in program execution (e.g., method call, execution, object creation).
  - **Pointcut**: An expression (e.g., `call(* *.*(..))`) that matches and captures `Join Point`s. Operators like `&&`, `||`, `!`, and wildcards like `*`, `+`, `..` are heavily used. Context functions like `target(obj)`, `args()`, `this(obj)` capture parameters and instances.
  - **Advice**: Code that executes `before()`, `after()`, or `around()` (replacing) a captured join point.
- **Tooling**: Requires an AspectJ compiler (Weaver) to inject aspects into compiled bytecode.

### 1.8 Messaging Patterns [Hohpe]
- **Event-Driven Consumer**: Consumers don't pull/spin for messages; they are awakened by the messaging system pushing messages via callbacks (`handleDelivery`).
- **Competing Consumers**: Multiple consumers bind to the same queue. The broker delivers messages in a round-robin load-balancing fashion. `basicQos(1)` is used to ensure a new message is not sent until the consumer sends an acknowledgment (`basicAck`).
- **Publish-Subscribe**: An event is broadcasted. A "Fanout" exchange copies the message to every bound queue, notifying multiple discrete subscriber systems.
- **Message Filter**: Only consumes specific messages from a stream using "Direct" or "Topic" exchanges with specific routing keys (e.g., `*.orange.*`).
- **RPC over Messaging**: Handled via `BasicProperties`, specifically the `replyTo` property specifying the callback queue and `correlationId` to map responses to requests.

### 1.9 Reference Monitor
- **Intent**: Enforce access restrictions by intercepting requests and checking compliance with authorizations.
- **Process**: Intercepts requests to a `ProtectionObject` using a Proxy, evaluates the `Request` against a `SetOfAuthorizationRules`, and passes valid requests through.

### 1.10 Remote Facade & Data Transfer Object (DTO)
- **Intent**: Provide coarse-grained interfaces over fine-grained objects to improve network efficiency, using DTOs to transfer multiple data points in a single call.
- **Benefits**: Reduces expensive inter-process calls. DTOs serialize data (e.g., via RMI) for network transport without sending complex DOM objects.

### 1.11 Session State Management
- **Client Session State**: Stateful data stored entirely on the client (URL, hidden fields, cookies). Good for stateless servers/failover, bad for large or sensitive data.
- **Server Session State**: Session state kept on server memory or a database (as pending data). Easy clustering/failover with database state.
- **Serialized LOB**: Storing a complex object graph as a single serialized Large Object (BLOB/CLOB) in a database column.

### 1.12 Idempotent Receiver
- **Intent**: Identify client requests uniquely to safely ignore duplicate requests (e.g., from network retries).
- **Process**: Uses a `MessageDB` storing unique message IDs. If an ID exists, the server returns the cached outcome instead of reprocessing.

### 1.13 Leader and Followers
- **Intent**: One server coordinates replication for a cluster. Followers forward client requests to the Leader. 
- **Core Concepts**:
  - **Majority Quorum**: Prevents split-brain by requiring a majority (`(n/2)+1`) of votes for consensus.
  - **Generation Clock**: Monotonically increasing counter updated at each leader election to reject requests from old/disconnected leaders.
  - **Heartbeat**: Periodic ping messages to detect server failures and trigger new elections.

### 1.14 Request Pipeline
- **Intent**: Improve latency by sending multiple requests on a connection without waiting for prior responses.
- **Process**: Needs limits on in-flight requests and unique IDs to map out-of-order responses (e.g., async RMI calls via `CompletableFuture`).

### 1.15 Request Batch
- **Intent**: Combine multiple small requests into a single batch to optimize network usage and reduce latency overhead.
- **Process**: A `RequestSender` accumulates requests and a `BatchAssembler` packages them into one network call. The server-side `BatchProcessor` handles the batch and returns an aggregated response, which a `ResponseSplitter` matches back to original requesters.

### 1.16 Resilience & Stability Patterns
- **Timeout**: Stops indefinite waiting on network/resources, isolating faults at integration points.
- **Circuit Breaker**: Shields failing operations by stopping calls completely ("Opening" the circuit) after a failure threshold (`Closed -> Open -> Half-Open`), avoiding cascading failures.
- **Bulkheads**: Partitioning resources (e.g., CPU, thread pools, independent VMs) to contain damage so one failing component doesn't sink the entire system.

---

## 2. Technologies and Best Practices

### 2.1 Password Hashing: Scrypt
- Passwords MUST be hashed uniquely so simple databases breaches don't result in plain-text leaks, minimizing brute force vulnerabilities.
- **Technology**: `Scrypt` algorithm via `SCryptUtil`.
- Features salt inclusion so same-passwords yield different hashes, destroying "Rainbow table" effectiveness.
- Scrypt is highly memory/CPU dependent by design, configured by parameters `N` (work factor), `r` (memory cost), and `p` (parallelization). For example: 100ms calculation time delays rapid cracking.
  
### 2.2 DDoS Gateway Protection: Guava RateLimiter
- Protect against Denial of Service attacks before hitting Authentication logic or business logic.
- **Technology / Implementation**: Deep defense via Google `Guava` library's `RateLimiter`.
- Example: `RateLimiter.create(limitPerSecond)`. Incoming requests call `acquire()` which blocks if limits are reached, or `tryAcquire(timeout)` which drops the request rapidly.
- Must be initialized at system entry points (Reverse Proxies/Gateways).

### 2.3 Single Sign-on / Proof of Identity: JSON Web Tokens (JWT)
- To prevent repeat logins (stateless servers).
- **Technology**: `auth0` (`java-jwt`) library.
- JSON structure comprising standard claims (`iss`: Issuer, `sub`: Subject, `exp`: Expiry time).
- Tokens are digitally signed using cryptography (e.g., `Algorithm.RSA256(publicKey, privateKey)`) ensuring tamper evidence. Re-authentication on API ends happens using verify-checks on these tokens mapping attributes without databases.

### 2.4 Message Broker: RabbitMQ
- Enables asynchronous, store-and-forward communication between polyglot microservices.
- Architecture:
  - **Exchanges**: (fanout, direct, topic, headers). Entrypoints where Producers `basicPublish` to.
  - **Queues**: Destinations buffers for messages. Usually created specific, private (`exclusive`, `auto-delete`), or durable depending on usage.
  - **Bindings**: Routes mapping exchanges to queues.
- Utilizes `amqp-client.jar`. Java clients support Automatic Connection Recovery mapping topology recovery dynamically during outages.
- Consumers implement graceful callback behavior returning explicit acknowledgements to prevent data loss.

### 2.5 Java CompletableFuture
- Enables asynchronous, non-blocking programming (replacing the older `Future` model).
- **Key methods**: `supplyAsync()` to start tasks, `thenApply()`, `thenAccept()`, `thenCombine()`, `thenCompose()` for chaining tasks without blocking. 
- Allows aggregation via `allOf()` and `anyOf()`, built-in timeouts (`orTimeout`, `completeOnTimeout`), and async exception handling (`completeExceptionally`).

### 2.6 Resilience4J
- A lightweight, functional fault tolerance library designed for Java 8 and functional programming.
- Provides higher-order functions (decorators) for `TimeLimiter`, `CircuitBreaker`, `Retry`, `Bulkhead`, and `RateLimiter`.
- **Configurability**: Allows fine-tuning of thresholds via `CircuitBreakerConfig` (e.g., sliding window sizes, failure rate limits, wait durations).
