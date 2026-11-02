# Inventory Service - Event-Driven Architecture 📦

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-green)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.1.1-blue)
![Kafka](https://img.shields.io/badge/Apache_Kafka-KRaft-black)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

This is the **Inventory Microservice**, the second participant in a **Choreography-based SAGA pattern**. 

It acts reactively: it listens for incoming orders from Kafka, checks product availability in its **PostgreSQL** database, and publishes success (`StockReserved`) or failure (`StockRejected`) events back to the system. It is designed with resilience in mind, utilizing **Dead Letter Queues (DLQ)** to prevent poisonous messages from blocking the consumer.

## 🏗 Architecture & Flow

The Inventory Service listens to the `orders-topic`. When an event arrives, it follows this flow:

```mermaid
sequenceDiagram
    participant Kafka as Apache Kafka
    participant IS as Inventory Service
    participant DB as PostgreSQL
    
    Kafka->>IS: Consume OrderCreatedEvent (orders-topic)
    IS->>DB: Check Stock for productId
    alt Stock >= Quantity
        IS->>DB: Update Stock (- quantity)
        IS->>Kafka: Publish StockReservedEvent (stock-reserved-topic)
    else Stock < Quantity OR Product not found
        IS->>Kafka: Publish StockRejectedEvent (stock-rejected-topic)
    end
