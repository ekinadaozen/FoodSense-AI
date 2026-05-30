# 📨 Kafka Flow — FoodSense AI

## Table of Contents

- [Why Kafka?](#-why-kafka)
- [Topic Configuration](#-topic-configuration)
- [Producer Flow](#-producer-flow)
- [Consumer Flow](#-consumer-flow)
- [Message Format](#-message-format)
- [Consumer Group Configuration](#-consumer-group-configuration)
- [Error Handling Strategy](#-error-handling-strategy)
- [Flow Diagram](#-flow-diagram)
- [Configuration Properties](#-configuration-properties)

---

## 🤔 Why Kafka?

Apache Kafka serves as the **message broker** between complaint submission and AI analysis. But why not just call the Gemini API directly from the service layer?

### The Problem Without Kafka

```mermaid
sequenceDiagram
    participant Customer
    participant API as REST API
    participant Gemini as Google Gemini

    Customer->>API: POST /api/complaints
    API->>Gemini: Analyze complaint (2-5 sec)
    Note over API,Gemini: Customer waits...
    Note over API,Gemini: What if Gemini is down?
    Note over API,Gemini: What if Gemini is slow?
    Gemini-->>API: Analysis result
    API-->>Customer: 201 Created (after 5+ seconds)
```

**Problems with direct calls:**
- ⏳ **Latency** — Customer waits 3-10 seconds for Gemini to respond
- 💥 **Failure cascade** — If Gemini is down, complaint submission fails entirely
- 🔗 **Tight coupling** — API server is directly dependent on Gemini availability
- 📈 **No scalability** — Each request blocks a thread waiting for Gemini

### The Solution With Kafka

```mermaid
sequenceDiagram
    participant Customer
    participant API as REST API
    participant Kafka as Kafka Topic
    participant Consumer as AI Consumer
    participant Gemini as Google Gemini

    Customer->>API: POST /api/complaints
    API->>Kafka: Publish complaint ID
    API-->>Customer: 201 Created (instant!)

    Note over Kafka,Consumer: Async processing

    Kafka->>Consumer: Deliver message
    Consumer->>Gemini: Analyze complaint
    Gemini-->>Consumer: Analysis result
    Consumer->>Consumer: Save to database
```

### Benefits of Using Kafka

| Benefit             | Description                                                    |
| ------------------- | -------------------------------------------------------------- |
| **Async Processing**| Customer gets instant response; AI runs in the background      |
| **Decoupling**      | API doesn't know about Gemini; Kafka bridges the gap           |
| **Resilience**      | If Gemini is down, messages queue up and are processed later   |
| **Scalability**     | Add more consumer instances to handle higher throughput        |
| **Reliability**     | Kafka persists messages to disk; no data loss on consumer crash |
| **Backpressure**    | Consumers process at their own pace, not overwhelmed by spikes |

> [!IMPORTANT]
> The key insight is **temporal decoupling** — the producer and consumer don't need to be active at the same time. The complaint can be submitted now and analyzed minutes later if needed.

---

## 📋 Topic Configuration

### Topic Details

| Property           | Value                | Description                           |
| ------------------ | -------------------- | ------------------------------------- |
| **Topic Name**     | `complaint-topic`    | The Kafka topic for complaint events  |
| **Partitions**     | 1                    | Single partition for ordered processing |
| **Replication Factor** | 1                | Single replica (dev environment)      |
| **Retention**      | 7 days (default)     | Messages retained for 7 days         |
| **Cleanup Policy** | `delete`             | Messages deleted after retention      |

### Topic Creation

The topic is auto-created via Spring Boot Kafka configuration:

```java
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic complaintTopic() {
        return TopicBuilder
            .name("complaint-topic")
            .partitions(1)
            .replicas(1)
            .build();
    }
}
```

> [!NOTE]
> In **production**, you'd increase partitions (for parallelism) and replicas (for fault tolerance). For example: 6 partitions, 3 replicas across a 3-broker cluster.

### Why 1 Partition?

For development and this portfolio project, 1 partition is sufficient because:
- We have a single consumer instance
- Message ordering is preserved (complaints analyzed in submission order)
- Simpler to debug and understand

**For production:** Multiple partitions allow multiple consumer instances to process messages in parallel:

```mermaid
graph LR
    P["Producer"] --> T["complaint-topic"]

    subgraph "1 Partition (Dev)"
        T --> C1["Consumer 1<br/>(processes all)"]
    end

    P2["Producer"] --> T2["complaint-topic"]

    subgraph "3 Partitions (Prod)"
        T2 --> P0["Partition 0"]
        T2 --> P1["Partition 1"]
        T2 --> P2b["Partition 2"]
        P0 --> C2["Consumer 1"]
        P1 --> C3["Consumer 2"]
        P2b --> C4["Consumer 3"]
    end
```

---

## 📤 Producer Flow

### How It Works

The `ComplaintProducer` publishes the complaint **UUID** (not the full object) to the Kafka topic after the complaint is saved to PostgreSQL.

```mermaid
flowchart LR
    A["ComplaintService<br/>createComplaint()"] -->|"complaint.getId()"| B["ComplaintProducer<br/>sendComplaintId()"]
    B -->|"kafkaTemplate.send()"| C["Kafka Topic<br/>complaint-topic"]

    style A fill:#E8F5E9,stroke:#4CAF50,color:#000
    style B fill:#C8E6C9,stroke:#388E3C,color:#000
    style C fill:#37474F,stroke:#FFF,color:#FFF
```

### Producer Code

```java
@Service
@Slf4j
public class ComplaintProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "complaint-topic";

    public ComplaintProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendComplaintId(String complaintId) {
        log.info("Publishing complaint ID to Kafka: {}", complaintId);
        kafkaTemplate.send(TOPIC, complaintId);
        log.info("Successfully published complaint ID: {}", complaintId);
    }
}
```

### Why Send Only the ID?

| Approach                | Pros                          | Cons                              |
| ----------------------- | ----------------------------- | --------------------------------- |
| **Send full object**    | Consumer has all data         | Large message size, serialization |
| **Send only ID** ✅     | Small messages, simple format | Consumer must query DB            |

We send **only the UUID** because:
1. **Small message size** — UUIDs are 36 characters vs. potentially large complaint text
2. **Single source of truth** — Database is the source of truth, not Kafka messages
3. **No serialization complexity** — Just a string, no custom serializer needed
4. **Data consistency** — Consumer always gets the latest data from the database

---

## 📥 Consumer Flow

### How It Works

The `ComplaintConsumer` listens to the `complaint-topic`, receives the complaint ID, fetches the full complaint from the database, sends it to Gemini for analysis, and saves the result.

```mermaid
flowchart TD
    A["Kafka Topic<br/>complaint-topic"] -->|"Deliver message"| B["ComplaintConsumer<br/>@KafkaListener"]
    B -->|"Parse UUID"| C{"Valid UUID?"}
    C -->|"Yes"| D["Fetch Complaint<br/>complaintRepository.findById()"]
    C -->|"No"| E["Log error<br/>Skip message"]
    D -->|"Found"| F["Extract complaint text"]
    D -->|"Not found"| G["Log warning<br/>Skip message"]
    F --> H["Call GeminiService<br/>analyzeComplaint()"]
    H -->|"Success"| I["Create ComplaintAnalysis entity"]
    H -->|"Failure"| J["Log error<br/>Update status to FAILED"]
    I --> K["Save analysis<br/>analysisRepository.save()"]
    K --> L["Update complaint status<br/>to ANALYZED"]

    style A fill:#37474F,stroke:#FFF,color:#FFF
    style B fill:#212121,stroke:#FFF,color:#FFF
    style C fill:#FFF9C4,stroke:#F9A825,color:#000
    style D fill:#BBDEFB,stroke:#1976D2,color:#000
    style F fill:#E8F5E9,stroke:#4CAF50,color:#000
    style H fill:#E1BEE7,stroke:#7B1FA2,color:#000
    style I fill:#C8E6C9,stroke:#388E3C,color:#000
    style K fill:#BBDEFB,stroke:#1976D2,color:#000
    style L fill:#BBDEFB,stroke:#1976D2,color:#000
    style E fill:#FFCDD2,stroke:#D32F2F,color:#000
    style G fill:#FFCDD2,stroke:#D32F2F,color:#000
    style J fill:#FFCDD2,stroke:#D32F2F,color:#000
```

### Consumer Code

```java
@Service
@Slf4j
public class ComplaintConsumer {

    private final ComplaintRepository complaintRepository;
    private final AnalysisRepository analysisRepository;
    private final GeminiService geminiService;

    @KafkaListener(topics = "complaint-topic", groupId = "complaint-analysis-group")
    public void consumeComplaintId(String complaintId) {
        log.info("Received complaint ID from Kafka: {}", complaintId);

        try {
            // 1. Parse UUID
            UUID id = UUID.fromString(complaintId);

            // 2. Fetch complaint from database
            Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Complaint not found: " + complaintId));

            // 3. Send to Gemini for analysis
            GeminiAnalysisResult result = geminiService.analyzeComplaint(
                complaint.getComplaintText());

            // 4. Create and save analysis entity
            ComplaintAnalysis analysis = ComplaintAnalysis.builder()
                .complaint(complaint)
                .category(result.getCategory())
                .sentiment(result.getSentiment())
                .priority(result.getPriority())
                .summary(result.getSummary())
                .analyzedAt(LocalDateTime.now())
                .build();

            analysisRepository.save(analysis);

            // 5. Update complaint status
            complaint.setStatus(ComplaintStatus.ANALYZED);
            complaintRepository.save(complaint);

            log.info("Analysis saved for complaint: {}", complaintId);

        } catch (Exception e) {
            log.error("Failed to process complaint: {}", complaintId, e);
            // Update status to FAILED if possible
            handleFailure(complaintId);
        }
    }
}
```

---

## 📦 Message Format

### Message Structure

| Property    | Value                                      |
| ----------- | ------------------------------------------ |
| **Topic**   | `complaint-topic`                          |
| **Key**     | `null` (not partitioned by key)            |
| **Value**   | Complaint UUID as a plain string           |
| **Format**  | Plain text (`String`)                      |
| **Example** | `f5e6d7c8-9a0b-1c2d-3e4f-567890abcdef`   |

### Serialization Configuration

| Component    | Serializer/Deserializer                    |
| ------------ | ------------------------------------------ |
| **Producer** | `StringSerializer` (key), `StringSerializer` (value) |
| **Consumer** | `StringDeserializer` (key), `StringDeserializer` (value) |

```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

---

## 👥 Consumer Group Configuration

### Consumer Group Details

| Property              | Value                         | Description                               |
| --------------------- | ----------------------------- | ----------------------------------------- |
| **Group ID**          | `complaint-analysis-group`    | Identifies this consumer group            |
| **Auto Offset Reset** | `earliest`                   | Start from beginning if no committed offset |
| **Concurrency**       | 1                            | Single consumer thread                    |
| **Auto Commit**       | `true` (default)             | Offsets committed automatically           |

### What Is a Consumer Group?

```mermaid
graph LR
    subgraph "Consumer Group: complaint-analysis-group"
        C1["Consumer Instance 1<br/>(current setup)"]
    end

    T["complaint-topic<br/>Partition 0"] --> C1

    subgraph "Scaled Consumer Group (future)"
        C2["Consumer Instance 1<br/>Partition 0, 1"]
        C3["Consumer Instance 2<br/>Partition 2, 3"]
        C4["Consumer Instance 3<br/>Partition 4, 5"]
    end
```

A **consumer group** ensures that each message is processed by **exactly one consumer** in the group. If you add more instances of the application, they join the same group and **split the work** across partitions.

> [!TIP]
> **Scaling rule:** The maximum number of consumers that can work in parallel equals the number of partitions. With 6 partitions, you can have up to 6 consumer instances processing concurrently.

---

## 🛡️ Error Handling Strategy

### Current Error Handling (v1.0.0)

```mermaid
flowchart TD
    A["Message received"] --> B{"Processing<br/>successful?"}
    B -->|"Yes"| C["Offset committed<br/>Message acknowledged"]
    B -->|"No"| D["Error logged"]
    D --> E["Complaint status<br/>set to FAILED"]
    E --> F["Offset committed<br/>Message skipped"]

    style C fill:#C8E6C9,stroke:#388E3C,color:#000
    style D fill:#FFCDD2,stroke:#D32F2F,color:#000
    style E fill:#FFCDD2,stroke:#D32F2F,color:#000
```

### Error Categories

| Error Type                  | Handling                                   | Retryable? |
| --------------------------- | ------------------------------------------ | ---------- |
| **Invalid UUID**            | Log error, skip message                    | No         |
| **Complaint not found**     | Log warning, skip message                  | No         |
| **Gemini API timeout**      | Log error, set status to `FAILED`          | Yes*       |
| **Gemini API rate limit**   | Log error, set status to `FAILED`          | Yes*       |
| **JSON parse error**        | Log error, set status to `FAILED`          | Yes*       |
| **Database error**          | Log error, message may be retried          | Yes        |

> [!WARNING]
> **Current limitation:** There is no automatic retry mechanism in v1.0.0. Failed messages are logged and skipped. A dead letter queue (DLQ) and retry strategy are planned for future releases.

### Planned Improvements (Future)

```mermaid
flowchart TD
    A["Message received"] --> B{"Processing<br/>successful?"}
    B -->|"Yes"| C["Offset committed"]
    B -->|"No"| D{"Retry count<br/>< 3?"}
    D -->|"Yes"| E["Wait and retry<br/>(exponential backoff)"]
    E --> A
    D -->|"No"| F["Send to Dead Letter Topic<br/>(complaint-topic.DLT)"]
    F --> G["Alert operations team"]

    style C fill:#C8E6C9,stroke:#388E3C,color:#000
    style E fill:#FFF9C4,stroke:#F9A825,color:#000
    style F fill:#FFCDD2,stroke:#D32F2F,color:#000
    style G fill:#FFCDD2,stroke:#D32F2F,color:#000
```

---

## 🔄 Flow Diagram — Complete Kafka Pipeline

```mermaid
graph TB
    subgraph "1. Complaint Submission"
        A["POST /api/complaints"] --> B["ComplaintService.createComplaint()"]
        B --> C["Save to PostgreSQL<br/>(status: PENDING)"]
        B --> D["ComplaintProducer.sendComplaintId()"]
        D --> E["KafkaTemplate.send()"]
    end

    subgraph "2. Kafka Broker"
        E --> F["complaint-topic<br/>Partition 0"]
        F -->|"Offset 0"| G["Message: 'f5e6d7c8-...'"]
    end

    subgraph "3. AI Analysis Pipeline"
        G --> H["ComplaintConsumer<br/>@KafkaListener"]
        H --> I["UUID.fromString(complaintId)"]
        I --> J["complaintRepository.findById(id)"]
        J --> K["geminiService.analyzeComplaint(text)"]
        K --> L["Parse JSON response"]
        L --> M["analysisRepository.save(analysis)"]
        M --> N["Update complaint status: ANALYZED"]
    end

    style A fill:#C8E6C9,stroke:#388E3C,color:#000
    style C fill:#BBDEFB,stroke:#1976D2,color:#000
    style D fill:#212121,stroke:#FFF,color:#FFF
    style F fill:#37474F,stroke:#FFF,color:#FFF
    style G fill:#37474F,stroke:#FFF,color:#FFF
    style H fill:#212121,stroke:#FFF,color:#FFF
    style K fill:#E1BEE7,stroke:#7B1FA2,color:#000
    style M fill:#BBDEFB,stroke:#1976D2,color:#000
    style N fill:#BBDEFB,stroke:#1976D2,color:#000
```

---

## ⚙️ Configuration Properties

### Complete Kafka Configuration

```yaml
# application.yml
spring:
  kafka:
    # Kafka broker connection
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

    # Producer configuration
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all                    # Wait for all replicas to acknowledge
      retries: 3                   # Retry failed sends 3 times
      properties:
        enable.idempotence: true   # Prevent duplicate messages

    # Consumer configuration
    consumer:
      group-id: complaint-analysis-group
      auto-offset-reset: earliest  # Read from beginning if no offset
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      properties:
        max.poll.interval.ms: 300000   # 5 min max processing time
        session.timeout.ms: 30000      # 30 sec heartbeat timeout
```

### Configuration Property Reference

| Property                 | Value              | Description                                      |
| ------------------------ | ------------------ | ------------------------------------------------ |
| `bootstrap-servers`      | `localhost:9092`   | Kafka broker addresses                           |
| `producer.acks`          | `all`              | Durability guarantee — all replicas acknowledge  |
| `producer.retries`       | `3`                | Retry failed sends                               |
| `consumer.group-id`      | `complaint-analysis-group` | Consumer group identifier              |
| `consumer.auto-offset-reset` | `earliest`     | Start from oldest unread message                 |
| `max.poll.interval.ms`   | `300000`           | Max time between polls before consumer is kicked |
| `session.timeout.ms`     | `30000`            | Heartbeat timeout for consumer liveness          |

### Docker Compose Kafka Configuration

```yaml
# docker-compose.yml (Kafka section)
kafka:
  image: confluentinc/cp-kafka:7.6.0
  depends_on:
    - zookeeper
  ports:
    - "9092:9092"
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9092
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
    KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
```

---

## 📚 Further Reading

- [Architecture Guide](ARCHITECTURE.md) — How Kafka fits into the overall system
- [Gemini Integration](GEMINI_INTEGRATION.md) — What happens after Kafka delivers the message
- [Deployment Guide](DEPLOYMENT.md) — Kafka configuration in production
