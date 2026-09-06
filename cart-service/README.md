# cart-service

The authenticated user's shopping cart. Owns the `ecommerce_cart` MySQL
database. Every route works off the JWT's userId — there is no `/{userId}`
in any cart URL, so a user can't even construct a request that touches
someone else's cart.

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/cart` | Get the current user's cart (auto-created empty on first access) |
| POST | `/api/cart/items` | Add an item (merges quantity if the product is already in the cart) |
| PUT | `/api/cart/items/{productId}` | Set an item's quantity |
| DELETE | `/api/cart/items/{productId}` | Remove an item |
| DELETE | `/api/cart` | Clear the cart |

## How pricing works

Cart items store only `productId` and `quantity` — never a cached price.
Every read calls **product-service** via OpenFeign (`ProductClient`) to get
the live price, discount, name, image, and availability, so the cart never
shows a stale price. If product-service is unreachable, a circuit-breaker
fallback (`ProductClientFallbackFactory`) marks that item unavailable
instead of failing the whole cart request.

## Run

Prerequisites: `service-registry`, `config-server`, MySQL, and
`product-service` (for enrichment — the cart still loads without it, items
just show as unavailable).

```bash
mvn clean install
mvn spring-boot:run
```

Runs on **http://localhost:8085**. Swagger UI: http://localhost:8085/swagger-ui.html

## Sample requests

```bash
curl http://localhost:8085/api/cart \
  -H "Authorization: Bearer <access-token>"

curl -X POST http://localhost:8085/api/cart/items \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"productId":"<productId>","quantity":2}'
```

## Tests

```bash
mvn test
```

Uses in-memory H2 — no local MySQL/Redis required for the test suite.
