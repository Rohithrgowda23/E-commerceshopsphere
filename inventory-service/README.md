# inventory-service

Stock levels and reservation. Owns the `ecommerce_inventory` MySQL
database. Reservation uses row-level pessimistic locking (`SELECT ... FOR
UPDATE` via `findByIdForUpdate`) plus optimistic `@Version` locking on the
entity so concurrent reservations for the same product can't oversell.

## Endpoints

| Method | Path | Auth |
|---|---|---|
| GET | `/api/inventory/{productId}` | Authenticated |
| POST | `/api/inventory/check` | Authenticated |
| POST | `/api/inventory/reserve` | Authenticated |
| POST | `/api/inventory/release` | Authenticated |
| POST | `/api/inventory/add` | ADMIN |
| POST | `/api/inventory/reduce/{orderId}` | ADMIN |

## How reservation works

1. order-service creates an order and either:
   - calls `POST /api/inventory/reserve` directly via OpenFeign for an
     immediate answer, **or**
   - publishes an `ORDER_CREATED` event to `order-events`, which this
     service's `OrderEventConsumer` picks up.
2. Both paths call the same `InventoryService.reserveStock(...)`, which is
   **idempotent per orderId** — a redelivered Kafka message or a retried
   Feign call is a safe no-op if that order already has reservations.
3. Each reserved `(orderId, productId, quantity)` is recorded in
   `stock_reservations` — this is what makes release/reduce precise (they
   don't have to trust whatever item list a later request claims).
4. On payment success, `POST /api/inventory/reduce/{orderId}` permanently
   clears the reservation. On payment failure or order cancellation,
   `POST /api/inventory/release` (or an `ORDER_CANCELLED` Kafka event)
   restores `availableQuantity`.
5. Every reserve/release publishes `INVENTORY_RESERVED` /
   `INVENTORY_RELEASED` / `INVENTORY_RESERVATION_FAILED` to
   `inventory-events` for order-service and notification-service to react to.

Kafka consumption retries 3 times (2s apart) before dead-lettering to
`order-events.DLT`.

## Run

Prerequisites: `service-registry`, `config-server`, MySQL, Kafka.

```bash
mvn clean install
mvn spring-boot:run
```

Runs on **http://localhost:8084**. Swagger UI: http://localhost:8084/swagger-ui.html

## Sample requests

```bash
curl -X POST http://localhost:8084/api/inventory/add \
  -H "Authorization: Bearer <admin-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"productId":"<productId>","quantity":100}'

curl -X POST http://localhost:8084/api/inventory/check \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":"<productId>","quantity":2}]}'
```

## Tests

```bash
mvn test
```

Uses in-memory H2 and an embedded Kafka broker — no local infrastructure
required for the test suite.
