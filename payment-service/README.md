# payment-service

Mock payment processing and refunds. Owns the `ecommerce_payment` MySQL
database. Designed so a real gateway (Stripe/Razorpay) can be dropped in
later with a one-class change.

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/payments` | Charge for an order (usually triggered automatically — see below) |
| GET | `/api/payments/{orderId}` | Get the payment for an order |
| POST | `/api/payments/{orderId}/refund` | Refund a successful payment |

## How payment is actually triggered

Customers don't normally call `POST /api/payments` themselves. When
order-service publishes `ORDER_CREATED` to `order-events`, this service's
`OrderEventConsumer` automatically attempts a charge. The REST endpoint
exists for manual/admin use and for retrying a payment — both paths call
the same `PaymentService.processPayment(...)`, which is **idempotent per
orderId**: a redelivered event or a retried call returns the existing
result instead of charging twice.

## The mock gateway — and swapping in a real one later

`PaymentGateway` is an interface with one implementation today,
`MockPaymentGateway`, which:
- succeeds most of the time, fails a configurable percentage
  (`payment.mock.failure-rate-percent`, default 10%) so the
  `PAYMENT_FAILED` path is exercised in normal local testing
- adds a small artificial delay (`payment.mock.simulated-latency-ms`)

`PaymentServiceImpl` depends only on the `PaymentGateway` interface.
Adding Stripe/Razorpay later means implementing that interface and
marking it `@Primary` (or swapping the bean) — **no changes** to the
service layer, controller, or Kafka event contracts.

## Outcome events

Every charge attempt publishes to `payment-events`:
- `PAYMENT_SUCCESS` → order-service confirms the order; inventory-service
  permanently deducts the reservation.
- `PAYMENT_FAILED` → order-service cancels the order and releases the
  stock reservation.

Kafka consumption of `order-events` retries 3× (2s apart) before
dead-lettering to `order-events.DLT`.

## Run

Prerequisites: `service-registry`, `config-server`, MySQL, Kafka.

```bash
mvn clean install
mvn spring-boot:run
```

Runs on **http://localhost:8087**. Swagger UI: http://localhost:8087/swagger-ui.html

## Tests

```bash
mvn test
```

The test profile sets `failure-rate-percent: 0` so deterministic
"success" tests aren't flaky, and separately drives the gateway mock
directly to test the failure path. Uses in-memory H2 and an embedded
Kafka broker — no local infrastructure required.
