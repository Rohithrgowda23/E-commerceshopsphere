# api-gateway

Spring Cloud Gateway — the single entry point for the React frontend. Routes
requests to downstream microservices via Eureka, validates JWTs on
protected routes, applies CORS, logs every request with a correlation id,
and is wired for Redis-backed rate limiting.

## Prerequisites

Start, in order:

1. `service-registry` (port 8761)
2. `config-server` (port 8888)
3. Redis (`localhost:6379`) — optional for Phase 1, required once rate
   limiting is enabled on a route.

## Run

```bash
mvn clean install
mvn spring-boot:run
```

Gateway listens on **http://localhost:8080**.

## Verify

```bash
curl http://localhost:8080/api/gateway/status
```

```json
{"service":"api-gateway","status":"UP"}
```

Once `product-service` (Phase 3) is running and registered with Eureka:

```bash
curl http://localhost:8080/api/products
```

will be routed there automatically — no JWT required (GET on the catalog
is public).

## JWT behavior

- Missing/invalid/expired `Authorization: Bearer <token>` on a protected
  route → `401` with a JSON error body and an `X-Auth-Error` header.
- On success, the gateway forwards `X-User-Id`, `X-User-Email`, and
  `X-User-Roles` headers to the downstream service. Downstream services
  still re-validate the raw JWT themselves and enforce role-based
  authorization — the gateway only proves "this request carried a
  currently-valid token."

## Public (no-JWT) endpoints

- `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`
- `GET /api/products/**` (writes to `/api/products/**` still require a
  valid ADMIN JWT — enforced at product-service)
- `/actuator/health`, `/api/gateway/status`

## Tests

```bash
mvn test
```

Uses the `test` Spring profile (`application-test.yml`) to disable the
Config Server / Eureka client so tests run without external infrastructure.
