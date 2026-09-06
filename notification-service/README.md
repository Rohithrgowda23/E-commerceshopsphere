# notification-service

Consumes every platform Kafka topic and logs a notification row per
relevant event. Owns the `ecommerce_notification` MySQL database. Per the
project spec this initially just **logs and stores** notifications rather
than actually sending email/SMS/push.

## What it listens to

| Topic | Event types handled |
|---|---|
| `user-events` | `USER_REGISTERED` → welcome notification |
| `order-events` | `ORDER_CREATED`, `ORDER_CANCELLED`, `ORDER_STATUS_CHANGED` |
| `payment-events` | `PAYMENT_SUCCESS`, `PAYMENT_FAILED` |
| `inventory-events` | `INVENTORY_RESERVATION_FAILED` |

Every consumer funnels into the same `NotificationService.logNotification(...)`,
which is **idempotent by source Kafka event id** — a redelivered message
never creates a duplicate row. Each topic's consumption retries 3× (2s
apart) before dead-lettering to `<topic>.DLT`.

**Note:** `PaymentEvent` and `InventoryEvent` don't carry a `userId` in
their published contracts (only `orderId`) — those two notification types
are logged with `userId` unset. Resolving the user would mean either a
synchronous call back to order-service on every event or enriching those
event contracts; both were left as a deliberate simplification for this
scope rather than adding a hidden cross-service dependency.

## Endpoints

| Method | Path | Auth |
|---|---|---|
| GET | `/api/notifications` | Authenticated (the caller's own notifications, paginated) |

There's no write endpoint — notifications are only ever created by
consuming Kafka events, never via REST.

## Run

Prerequisites: `service-registry`, `config-server`, MySQL, Kafka.

```bash
mvn clean install
mvn spring-boot:run
```

Runs on **http://localhost:8088**. Swagger UI: http://localhost:8088/swagger-ui.html

## Tests

```bash
mvn test
```

Uses in-memory H2 and an embedded Kafka broker with all four topics (plus
their `.DLT` counterparts) pre-declared — no local infrastructure required.
