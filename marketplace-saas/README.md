## Marketplace SaaS (Multi-tenant)

Reference architecture for a multi-tenant SaaS marketplace, organized as a Maven monorepo. Services cover identity/auth, API gateway, order and payment domains, with observability and messaging foundations.

### Table of Contents
- Overview & stack
- Repository structure
- Quickstart (2 minutes)
- Run locally (with and without Docker)
- Identity (OAuth2/OIDC, JWT, JWKS)
- API Gateway
- Tests
- Configuration & profiles
- Roadmap
- Troubleshooting

### Overview & stack
- Language/Runtime: Java 21
- Frameworks: Spring Boot 3, Spring Authorization Server, Spring Cloud Gateway
- Build: Maven 3.9+
- Security: OAuth2/OIDC, JWT (RS256), JWKS
- Observability: Spring Boot Actuator (OTel pluggable)

### Repository structure (main packages)
- `packages/identity` (port 8081): Authorization Server (OAuth2/OIDC)
- `packages/api-gateway` (port 8080): Gateway/routing (foundation for per-tenant rate limit)
- `packages/orders` (port 8082): orders domain
- `packages/payments` (port 8083): payments domain
- `packages/delivery-dispatch` (port 8084): delivery domain

Each service has `src/main/resources/application.yml` with default ports and properties. Several services point to local Postgres and Kafka (see “Run with Docker”).

### Quickstart (2 minutes)
1) Build without tests:
```
mvn -q -DskipTests package
```
2) Start Identity and API Gateway (minimal auth path):
```
mvn -pl packages/identity spring-boot:run
mvn -pl packages/api-gateway spring-boot:run
```
3) Get a token via Client Credentials (Identity runs on 8081 by default):
```
curl -u test-client:test-secret \
  -d "grant_type=client_credentials&scope=payments:write" \
  http://localhost:8081/oauth2/token
```
4) JWKS (JWT public keys):
```
curl http://localhost:8081/.well-known/jwks.json
```

### Run locally with Docker (optional infra)
For domain services relying on Postgres/Redis/Kafka you can spin up local dependencies:

Postgres + Redis:
```
docker run -d --name pg -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=marketplace postgres:16
docker run -d --name redis -p 6379:6379 redis:7
```

Kafka (port 29092 to match default configs):
- Use your local Kafka or a ready-made stack (confluentinc, bitnami, redpanda). Example (bitnami compose):
```
curl -sSL https://raw.githubusercontent.com/bitnami/containers/main/bitnami/kafka/docker-compose.yml -o kafka-compose.yml
```
Edit `kafka-compose.yml` to expose `29092:29092` (outside/inside listener) and start it:
```
docker compose -f kafka-compose.yml up -d
```

Then run domain services as needed:
```
mvn -pl packages/orders spring-boot:run
mvn -pl packages/payments spring-boot:run
mvn -pl packages/delivery-dispatch spring-boot:run
```

### Run locally without Docker
- Identity + Gateway can run without external dependencies.
- Identity references a DataSource in `application.yml`. To run it without Postgres, disable JPA/DataSource auto-config:
```
mvn -pl packages/identity spring-boot:run -Dspring-boot.run.arguments="--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
```

### Identity (OAuth2/OIDC)
- Default in-memory client:
  - clientId: `test-client`
  - clientSecret: `test-secret`
  - grant types: `client_credentials` (foundation to add `authorization_code`)
  - scopes: `orders:write`, `payments:write`, `payments:refund`
- Main endpoints:
  - `/oauth2/token` (token issuance)
  - `/.well-known/jwks.json` (JWKS)
- Tokens are signed with RS256 (RSA key generated in-memory at boot).

Example token (Client Credentials):
```
curl -u test-client:test-secret \
  -d "grant_type=client_credentials&scope=payments:write" \
  http://localhost:8081/oauth2/token
```

### API Gateway
- Default port: 8080
- Identity route examples: `/v1/auth/**` and `/v1/users/**` → `http://localhost:8081`
- Basis for Redis-backed per-tenant rate limiting (profile `redis-rl`). To enable:
```
mvn -pl packages/api-gateway spring-boot:run -Dspring-boot.run.profiles=redis-rl
```
Ensure Redis is running (see Docker section).

### Tests
- All services:
```
mvn test
```
- Identity tests only:
```
mvn -pl packages/identity test
```

### Configuration & profiles
- Per-service `application.yml` in `src/main/resources`.
- Test profiles commonly use `application-test.yml`.
- Useful flags:
  - `server.port` to switch ports quickly.
  - Disable DB in Identity: `spring.autoconfigure.exclude` (see “without Docker”).

### Roadmap
- Gateway: per-tenant rate limit (Redis) and canary release.
- Identity: `authorization_code`, in-memory users RBAC, basic TOTP MFA, persistent JWKS.
- Orders: extra RFC7807, ETag/If-None-Match, cursor-based pagination.
- Persistence/messaging: Postgres (schemas with RLS footprint), Kafka (Outbox/Sagas), Redis, OpenSearch; tests with Testcontainers.
- Observability: OTel exporter/collector and metrics; metric smoke tests.
- Performance: k6 scripts.
- Contracts: per-service OpenAPI and contract tests.
- CI/CD: GitHub Actions; basic chaos scenarios.

### Troubleshooting
- Maven warning about missing Spring Boot plugin `<version>`: build works, but pin versions in module or parent `pom`.
- `Connection refused` for Postgres/Kafka/Redis: check containers/ports and credentials.
- To run Identity without a local DB, use the auto-config exclusion flag (see above).

