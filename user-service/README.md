# user-service

User profile and address management. Owns the `ecommerce_user` MySQL
database. Consumes `UserRegisteredEvent` from the `user-events` Kafka topic
(published by auth-service) to create the initial profile row.

## Endpoints

| Method | Path | Auth |
|---|---|---|
| GET | `/api/users/{id}` | Owner or ADMIN |
| PUT | `/api/users/{id}` | Owner or ADMIN |
| GET | `/api/users/{id}/addresses` | Owner or ADMIN |
| POST | `/api/users/{id}/addresses` | Owner or ADMIN |
| PUT | `/api/users/{id}/addresses/{addressId}` | Owner or ADMIN |
| DELETE | `/api/users/{id}/addresses/{addressId}` | Owner or ADMIN |

Every request must carry a valid `Authorization: Bearer <token>` header.
The service re-validates the JWT itself (independent of the gateway) and
rejects any request where the authenticated user is neither the resource
owner nor an ADMIN, with a `403`.

## Run

Prerequisites: `service-registry`, `config-server`, MySQL, Kafka.

```bash
mvn clean install
mvn spring-boot:run
```

Runs on **http://localhost:8082**. Swagger UI: http://localhost:8082/swagger-ui.html

## How a profile gets created

1. A user registers via `POST /api/auth/register` on **auth-service**.
2. auth-service publishes a `UserRegisteredEvent` to the `user-events` Kafka topic.
3. user-service's `UserEventConsumer` picks it up and creates the matching
   `UserProfile` row — idempotently (a redelivered/duplicate event is a no-op
   if the profile already exists).
4. Failed processing retries 3 times (2s apart) before the message is
   routed to the `user-events.DLT` dead-letter topic instead of being lost
   or blocking the partition.

## Sample requests

```bash
curl http://localhost:8082/api/users/<userId> \
  -H "Authorization: Bearer <access-token>"

curl -X PUT http://localhost:8082/api/users/<userId> \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","phoneNumber":"+15551234567","preferredLanguage":"EN","marketingOptIn":true}'

curl -X POST http://localhost:8082/api/users/<userId>/addresses \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"label":"Home","recipientName":"Jane Doe","phoneNumber":"+15551234567","addressLine1":"123 Main St","city":"Springfield","state":"IL","postalCode":"62704","country":"USA","isDefault":true}'
```

## Tests

```bash
mvn test
```

Uses in-memory H2 (MySQL compatibility mode) and an embedded Kafka broker —
no local infrastructure required for the test suite.
