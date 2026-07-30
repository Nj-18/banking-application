# AGENTS.md

## Cursor Cloud specific instructions

### Product overview

Single Spring Boot 3.5 REST API (`banking`) for customer, account, and transaction management. No frontend. See `README.md` for feature list.

### Required services

| Service | Port | Notes |
|---------|------|-------|
| MySQL 8 | 3306 | Database `banking_db`; credentials in `src/main/resources/application.properties` |
| Spring Boot API | 8080 | `./mvnw spring-boot:run` from repo root |

### MySQL startup

MySQL is a system service and is **not** started by the VM update script. If the API fails with a database connection error:

```bash
sudo service mysql start
```

The database and schema are created automatically on first app/test run (`ddl-auto=update`). Root password must match `application.properties` (`Smartnj18@`).

**Note:** The `mysql` CLI may fail with socket permission errors for the `ubuntu` user; the Spring Boot app connects via TCP (`jdbc:mysql://localhost:3306/...`) and works without socket access.

### Common commands

| Task | Command |
|------|---------|
| Download deps | `./mvnw dependency:resolve` |
| Run tests | `./mvnw test` |
| Build | `./mvnw package` |
| Run API (dev) | `./mvnw spring-boot:run` |

Ensure `mvnw` is executable (`chmod +x mvnw`) if you see "Permission denied".

### API smoke test

```bash
# Register customer
curl -s -X POST http://localhost:8080/api/customer/ \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com","mobileNumber":"555-0100","customerStatus":"ACTIVE"}'

# Create account (use customer id from above)
curl -s -X POST http://localhost:8080/api/accounts/ \
  -H 'Content-Type: application/json' \
  -d '{"customerId":1,"accountType":"SAVINGS","openingBalance":1000.0}'
```

### Linting

No linter or formatter is configured in this repo (no Checkstyle, SpotBugs, or similar in `pom.xml`). Validation is via `./mvnw test` and manual API testing.
