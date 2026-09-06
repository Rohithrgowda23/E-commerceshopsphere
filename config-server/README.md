# config-server

Centralized configuration server. Uses the Spring Cloud Config **native**
profile, serving YAML files from `src/main/resources/config-repo` — no Git
server or Docker required.

## Run

```bash
mvn clean install
mvn spring-boot:run
```

Start **after** `service-registry`.

## Verify

```bash
curl http://localhost:8888/api-gateway/default
```

should return the merged `application.yml` + `api-gateway.yml` configuration.

## Adding a new service's configuration

Drop a `<service-name>.yml` file into `src/main/resources/config-repo/`.
Any property also needs to exist in the client service's own
`bootstrap.yml`/`application.yml` under `spring.application.name` matching
the file name.
