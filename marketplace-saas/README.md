## Marketplace SaaS (Multi-tenant)

Projeto multi-módulos com serviços de exemplo para um marketplace SaaS. Inclui serviços como `identity`, `api-gateway`, `orders`, `payments`, `delivery-dispatch`, entre outros.

### Arquitetura (visão geral)
- Identity (OAuth2/OIDC Authorization Server) com JWT (RS256) e clientes in-memory.
- API Gateway (roteamento, base para rate limit por tenant).
- Serviços de domínio: Orders, Payments, Delivery.
- Observabilidade base via Actuator (OTel pode ser acoplado).
- Testes automatizados por serviço (JUnit + Spring Boot Test).

### Requisitos
- Java 21
- Maven 3.9+

### Compilar tudo
```
mvn -q -DskipTests package
```

### Rodar testes de todos os módulos
```
mvn test
```

### Serviço Identity
Implementa Authorization Server usando Spring Authorization Server.

- Client in-memory padrão:
  - clientId: `test-client`
  - clientSecret: `test-secret`
  - grant types: `client_credentials` (exemplo pronto). Base para `authorization_code`.
  - scopes: `orders:write`, `payments:write`, `payments:refund`

Executar somente o Identity:
```
mvn -pl packages/identity spring-boot:run
```

Obter token (Client Credentials):
```
curl -u test-client:test-secret \
  -d "grant_type=client_credentials&scope=payments:write" \
  http://localhost:8080/oauth2/token
```

JWKS:
```
curl http://localhost:8080/.well-known/jwks.json
```

Testes do Identity:
```
mvn -pl packages/identity test
```

### Executar serviços de domínio
Exemplos:
```
mvn -pl packages/orders spring-boot:run
mvn -pl packages/payments spring-boot:run
mvn -pl packages/delivery-dispatch spring-boot:run
```

### API Gateway
Executar:
```
mvn -pl packages/api-gateway spring-boot:run
```

### Configurações
- Cada serviço possui `application.yml` em `src/main/resources` com portas e propriedades básicas.
- Perfis de teste usam `application-test.yml` quando aplicável.

### Roadmap/Trabalhos futuros
- Gateway: rate limit por tenant via Redis e canary release.
- Identity: flows `authorization_code`, RBAC usuários in-memory, MFA TOTP básico, JWKS persistente.
- Orders: RFC7807 extra, ETag/If-None-Match, paginação por cursor.
- Persistência/mensageria: Postgres (schemas com RLS footprint), Kafka (Outbox/Sagas), Redis, OpenSearch; testes com Testcontainers.
- Observabilidade: Exporter OTel/Collector e métricas; smoke tests de métricas.
- Performance: scripts k6.
- Contratos: OpenAPI por serviço e contract tests.
- CI/CD: GitHub Actions; caos engineering básico.

### Troubleshooting
- Maven indicando versão ausente do plugin Spring Boot: o build funciona, mas recomenda-se definir `<version>` do plugin em cada `pom.xml` ou no `pom` pai.
- Problema com JPA em testes de serviços sem DB: use `application-test.yml` para excluir `DataSourceAutoConfiguration`/`HibernateJpaAutoConfiguration` quando não necessário.


