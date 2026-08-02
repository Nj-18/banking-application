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
- MySQL
- Maven
- Lombok
- Angular 19

## Run locally

### Backend

```bash
./mvnw spring-boot:run
```

API base: `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install
npm start
```

UI: `http://localhost:4200` (proxies `/api` to the backend)

## Future Enhancements

- Redis
- Kafka
- Docker
- Kubernetes
- AWS Deployment
- Prometheus & Grafana
