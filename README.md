# 📦 Inventory Service

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.8-green)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.0.0-blue)
![Kafka](https://img.shields.io/badge/Apache_Kafka-3.9.1-black)

> 📌 **Part of the [Event-Driven E-Commerce Architecture](https://github.com/andres-figueroa-luna/event-driven-microservices-portfolio).**
> Clone the umbrella repository to run the complete ecosystem with Docker Compose, PostgreSQL, and Kafka.

## 🎯 Overview
The **Inventory Service** is a pure event-driven consumer and producer. It does not expose endpoints for order creation; instead, it listens to Kafka topics to manage stock levels reactively. It validates stock availability and publishes success or failure events back to the Order Service, completing the SAGA.

## 🔌 API Endpoints (Product Management)

### Create Product
`POST /api/products`
```json
{
    "sku": "LAPTOP-ROG-001",
    "stock": 10
}
```

### Get Product by ID
`GET /api/products/{id}`

### List Products (Paginated)
`GET /api/products?page=0&size=10`

### Update Stock
`PUT /api/products/{id}/stock`
```json
{
    "stock": 50
}
```

## 🏗️ Architecture & Tech Stack
*   **Spring Boot 3.5.8 (LTS)** & **Java 21**
*   **Spring Cloud Stream** (Kafka Binder with Dead Letter Queues)
*   **Spring Data JPA** (PostgreSQL)
*   **Swagger / OpenAPI** for REST documentation.

## 🗄️ Database Schema
Hibernate automatically manages the following table (`ddl-auto=update`):
*   `products`: Stores product SKUs and available stock.

## 🚀 Running Locally
You can run this service standalone for local development:
```bash
mvn spring-boot:run
```
*Note: Requires a local PostgreSQL instance on port `5433` and Kafka on port `9092`.*

## 🧪 Testing
The project uses **Testcontainers** to run integration tests against real PostgreSQL and Kafka instances.
```bash
mvn clean verify
```