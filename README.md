# Banking Application

A production-grade banking application built using Spring Boot.

## Features

- Customer Management
- Bank Account Management
- Deposit Money
- Withdraw Money
- Transfer Money
- Transaction History
- Transaction History Filtering & Sorting by Date
- Global Exception Handling
- DTO & Mapper Pattern
- Spring Data JPA
- MySQL

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Lombok

## Transaction History API

Fetch transactions for an account. Results can be filtered by date range and sorted by transaction date.

```
GET /api/transactions/{accountNumber}
GET /api/transactions/{accountNumber}?sort=asc
GET /api/transactions/{accountNumber}?fromDate=2024-01-01T00:00:00&toDate=2024-12-31T23:59:59
GET /api/transactions/{accountNumber}?fromDate=2024-01-01T00:00:00&toDate=2024-06-30T23:59:59&sort=desc
```

| Query Param | Required | Default | Description |
|-------------|----------|---------|-------------|
| `fromDate`  | No       | —       | Inclusive start (`ISO-8601` date-time) |
| `toDate`    | No       | —       | Inclusive end (`ISO-8601` date-time) |
| `sort`      | No       | `desc`  | `asc` or `desc` by `transactionDate` |

## Future Enhancements

- JWT Authentication
- Redis
- Kafka
- Docker
- Kubernetes
- AWS Deployment
- Prometheus & Grafana
