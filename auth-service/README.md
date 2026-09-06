# auth-service

Registration, login, JWT issuance/refresh, and logout. Owns the
`ecommerce_auth` MySQL database. Publishes `UserRegisteredEvent` to the
`user-events` Kafka topic on successful registration.

## Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create a new USER account |
| POST | `/api/auth/login` | Public | Exchange credentials for access + refresh tokens |
| POST | `/api/auth/refresh` | Public (valid refresh token required) | Rotate an access/refresh token pair |
| POST | `/api/auth/logout` | Authenticated | Revoke a refresh token |

## Run

Prerequisites: `service-registry`, `config-server`, MySQL running locally, Kafka running locally (for event publishing — the app still starts without it, but publishing will log errors until Kafka is up).

```bash
mvn clean install
mvn spring-boot:run
```

Runs on **http://localhost:8081**. Swagger UI: http://localhost:8081/swagger-ui.html

## Sample requests

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"password123","firstName":"Jane","lastName":"Doe"}'

curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"password123"}'

curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token-from-login>"}'

curl -X POST http://localhost:8081/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

## Notes

- Passwords hashed with BCrypt (strength 12). Never logged, never returned in responses.
- Access tokens are short-lived (15 min) and stateless — contain `userId` (subject), `email`, `roles`.
- Refresh tokens are long-lived (7 days), stored in the `refresh_tokens` table, and **rotated** on every use (old one is revoked when a new pair is issued) so a leaked refresh token has a limited window of use.
- `ddl-auto: update` is used for local development convenience; a real deployment would use versioned migrations (Flyway/Liquibase).

## Tests

```bash
mvn test
```

Runs against an in-memory H2 database in MySQL compatibility mode — no local MySQL/Kafka/Eureka required for the test suite.
