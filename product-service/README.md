# product-service

Product catalog and categories. Owns the `ecommerce_product` MySQL
database. Reads are cached in Redis; writes evict the cache.

## Endpoints

| Method | Path | Auth |
|---|---|---|
| GET | `/api/products` | Public |
| GET | `/api/products/{id}` | Public |
| GET | `/api/products/search` | Public |
| GET | `/api/products/category/{categoryId}` | Public |
| POST / PUT / DELETE | `/api/products/**` | ADMIN |
| GET | `/api/categories`, `/api/categories/{id}` | Public |
| POST / PUT / DELETE | `/api/categories/**` | ADMIN |

## Search & filtering

```
GET /api/products/search?keyword=phone&categoryId=cat-1&brand=Acme&minPrice=100&maxPrice=500&page=0&size=20&sortBy=price&direction=asc
```

All query params are optional and composable — built dynamically with
Spring Data JPA Specifications (`ProductSpecifications`) rather than one
repository method per filter combination.

## Run

Prerequisites: `service-registry`, `config-server`, MySQL, Redis.

```bash
mvn clean install
mvn spring-boot:run
```

Runs on **http://localhost:8083**. Swagger UI: http://localhost:8083/swagger-ui.html

## Redis caching

- `products` cache: keyed by product id, 10-minute TTL, evicted entirely
  on any create/update/delete (simple invalidation — correctness over
  partial-cache cleverness for a catalog of this size).
- `categories` cache: same pattern, since categories change rarely.

## Sample requests

```bash
curl http://localhost:8083/api/products?page=0&size=10

curl -X POST http://localhost:8083/api/products \
  -H "Authorization: Bearer <admin-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Mouse","description":"Ergonomic wireless mouse","price":29.99,"categoryId":"<categoryId>","brand":"Acme","available":true}'
```

## Tests

```bash
mvn test
```

Uses in-memory H2 and Spring's simple in-memory cache — no local
MySQL/Redis required for the test suite.
