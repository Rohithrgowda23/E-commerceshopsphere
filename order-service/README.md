# order-service

Order placement, history, and cancellation. Owns the `ecommerce_order`
MySQL database. This is the most connected service in the platform —
it talks synchronously to three services and both produces and consumes
Kafka events.

## Endpoints

| Method | Path | Auth |
|---|---|---|
| POST | `/api/orders` | Authenticated (places an order from the caller's cart) |
| GET | `/api/orders` | Authenticated (the caller's own orders, paginated) |
| GET | `/api/orders/{id}` | Owner or ADMIN |
| PUT | `/api/orders/{id}/cancel` | Owner or ADMIN |

## Checkout flow

`POST /api/orders` takes **only shipping details** in the body — never
items. On call:

1. Validates the user via **user-service** (OpenFeign).
2. Pulls the live cart via **cart-service** (OpenFeign) — empty cart or
   any unavailable item rejects with `400`.
3. Checks stock via **inventory-service** (`POST /api/inventory/check`) —
   insufficient stock rejects with `409`.
4. Saves the order, **snapshotting** each item's name and price from the
   cart at that instant (so later price changes never retroactively
   change what the customer paid).
5. Reserves stock via **inventory-service** (`POST /api/inventory/reserve`)
   synchronously — this is a second, authoritative check that catches a
   race where stock was consumed by another order between steps 3 and 5;
   on conflict the order is marked `CANCELLED` and a `409` is returned.
6. Clears the cart via **cart-service**.
7. Publishes `ORDER_CREATED` to `order-events` for payment-service,
   notification-service, and inventory-service's async consumer (a no-op
   there since the reservation already happened synchronously).

Every outgoing Feign call forwards the placing user's original
`Authorization` header (see `FeignConfig`) — these services act on behalf
of that user, not as an anonymous internal caller.

## Reacting to the rest of the platform

- **`payment-events`** (`PaymentEventConsumer`): `PAYMENT_SUCCESS` →
  order `CONFIRMED`; `PAYMENT_FAILED` → order `CANCELLED` + stock released
  via inventory-service. Both are idempotent against redelivery.
- **`inventory-events`** (`InventoryEventConsumer`): a safety net for
  `INVENTORY_RESERVATION_FAILED` on the async path.

Kafka consumption retries 3× (2s apart) before dead-lettering to
`payment-events.DLT` / `inventory-events.DLT`.

## Run

Prerequisites: `service-registry`, `config-server`, MySQL, Kafka, and
`user-service`, `cart-service`, `inventory-service` running (checkout
calls all three).

```bash
mvn clean install
mvn spring-boot:run
```

Runs on **http://localhost:8086**. Swagger UI: http://localhost:8086/swagger-ui.html

## Sample request

```bash
curl -X POST http://localhost:8086/api/orders \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"shippingRecipientName":"Jane Doe","shippingPhoneNumber":"+15551234567","shippingAddressLine1":"123 Main St","shippingCity":"Springfield","shippingState":"IL","shippingPostalCode":"62704","shippingCountry":"USA"}'
```

## Tests

```bash
mvn test
```

Uses in-memory H2 and an embedded Kafka broker — no local infrastructure
required for the test suite.
