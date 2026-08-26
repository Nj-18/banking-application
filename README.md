# Banking Application

A production-grade banking application built using Spring Boot and Angular.

## Features

- Customer Management
- Bank Account Management
- Deposit Money
- Withdraw Money
- Transfer Money
- Transaction History
- JWT Authentication
- Angular frontend (Northline)
- Resilience4j (circuit breaker, retry, rate limiter, bulkhead)
- Global Exception Handling
- DTO & Mapper Pattern
- Spring Data JPA
- MySQL

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- Spring Security + JWT
- Resilience4j
- MySQL
- Maven
- Lombok
- Angular 19

## Resilience

| Pattern | Where | Purpose |
|--------|--------|---------|
| Circuit Breaker | account / transaction / customer services | Trip open on repeated failures |
| Retry | read APIs only (history, statement, customers) | Recover from transient DB blips |
| Rate Limiter | deposit / withdraw / transfer | Protect money-movement endpoints |
| Bulkhead | transfer | Limit concurrent transfers |

Money-movement APIs are **not** retried (avoids double-posting). Fallbacks return HTTP `503` / `429` with clear messages.

Actuator (local):
- `http://localhost:8081/actuator/health`
- `http://localhost:8081/actuator/circuitbreakers`

## Run locally

### Backend

```bash
./mvnw spring-boot:run
```

API base: `http://localhost:8081`

### Frontend

```bash
cd frontend
npm install
npm start
```

UI: `http://localhost:4200` (proxies `/api` to `:8081`)

## Future Enhancements

- Redis
- Kafka
- Docker
- Kubernetes
- AWS Deployment
- Prometheus & Grafana
