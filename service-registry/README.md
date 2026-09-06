# service-registry

Eureka Service Discovery server for the e-commerce microservices platform.

## Run

```bash
mvn clean install
mvn spring-boot:run
```

Dashboard: http://localhost:8761

All other services must be started **after** this one, since they register
with it on startup.
