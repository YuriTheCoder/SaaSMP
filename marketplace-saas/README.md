## Marketplace SaaS (Multi-tenant)

Arquitetura de referência para um marketplace SaaS multi-tenant, organizada em monorepo Maven. Os serviços cobrem autenticação/identidade, gateway, domínios de pedidos e pagamentos, e base para observabilidade e mensageria.

### Sumário
- Visão geral e stack
- Estrutura do repositório
- Quickstart (2 minutos)
- Executar localmente (com e sem Docker)
- Identity (OAuth2/OIDC, JWT, JWKS)
- API Gateway
- Testes
- Configuração e perfis
- Roadmap
- Troubleshooting

### Visão geral e stack
- Linguagem/Runtime: Java 21
- Framework: Spring Boot 3, Spring Authorization Server, Spring Cloud Gateway
- Build: Maven 3.9+
- Segurança: OAuth2/OIDC, JWT (RS256), JWKS
- Observabilidade: Spring Boot Actuator (OTel plugável)

### Estrutura do repositório (principais pacotes)
- `packages/identity` (porta 8081): Authorization Server (OAuth2/OIDC)
- `packages/api-gateway` (porta 8080): Gateway/roteamento (base para rate limit por tenant)
- `packages/orders` (porta 8082): domínio de pedidos
- `packages/payments` (porta 8083): domínio de pagamentos
- `packages/delivery-dispatch` (porta 8084): domínio de entregas

Cada serviço possui `src/main/resources/application.yml` com portas e propriedades padrão. Vários serviços apontam para Postgres e Kafka locais (veja "Executar com Docker").

### Quickstart (2 minutos)
1) Compilar sem testes:
```
mvn -q -DskipTests package
```
2) Subir só Identity e Gateway (mínimo para autenticação):
```
mvn -pl packages/identity spring-boot:run
mvn -pl packages/api-gateway spring-boot:run
```
3) Obter token via Client Credentials (Identity roda por padrão na 8081; o exemplo abaixo chama direto o Authorization Server):
```
curl -u test-client:test-secret \
  -d "grant_type=client_credentials&scope=payments:write" \
  http://localhost:8081/oauth2/token
```
4) JWKS (chaves públicas do JWT):
```
curl http://localhost:8081/.well-known/jwks.json
```

### Executar localmente com Docker (infra opcional)
Para rodar serviços de domínio que dependem de Postgres/Redis/Kafka, você pode levantar dependências locais:

Docker (Postgres + Redis rápidos):
```
docker run -d --name pg -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=marketplace postgres:16
docker run -d --name redis -p 6379:6379 redis:7
```

Kafka (porta 29092 para alinhar com a config padrão):
- Use seu Kafka local preferido ou uma stack pronta (confluentinc, bitnami, redpanda). Ex.: bitnami (compose):
```
curl -sSL https://raw.githubusercontent.com/bitnami/containers/main/bitnami/kafka/docker-compose.yml -o kafka-compose.yml
```
Edite `kafka-compose.yml` para expor `29092:29092` (listener inside/outside) e suba:
```
docker compose -f kafka-compose.yml up -d
```

Depois, rode os serviços de domínio conforme necessário:
```
mvn -pl packages/orders spring-boot:run
mvn -pl packages/payments spring-boot:run
mvn -pl packages/delivery-dispatch spring-boot:run
```

### Executar localmente sem Docker
- Para executar apenas Identity e Gateway, nenhuma dependência externa é necessária.
- Identity referencia um DataSource no `application.yml`. Se você quiser rodar sem Postgres, pode desabilitar auto-config JPA/Datasource com:
```
mvn -pl packages/identity spring-boot:run -Dspring-boot.run.arguments="--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
```

### Identity (OAuth2/OIDC)
- Client in-memory padrão:
  - clientId: `test-client`
  - clientSecret: `test-secret`
  - grant types: `client_credentials` (base pronta para adicionar `authorization_code`)
  - scopes: `orders:write`, `payments:write`, `payments:refund`
- Endpoints principais:
  - `/oauth2/token` (emissão de token)
  - `/.well-known/jwks.json` (JWKS)
- Tokens assinados com RS256 (chave RSA gerada em memória no boot).

Exemplo token (Client Credentials):
```
curl -u test-client:test-secret \
  -d "grant_type=client_credentials&scope=payments:write" \
  http://localhost:8081/oauth2/token
```

### API Gateway
- Porta padrão: 8080
- Rota Identity (ex.): `/v1/auth/**` e `/v1/users/**` → `http://localhost:8081`
- Base para rate limit por tenant via Redis (perfil `redis-rl`). Para ativar:
```
mvn -pl packages/api-gateway spring-boot:run -Dspring-boot.run.profiles=redis-rl
```
Certifique-se de ter o Redis rodando (veja seção Docker acima).

### Testes
- Testes de todos os serviços:
```
mvn test
```
- Testes do Identity:
```
mvn -pl packages/identity test
```

### Configuração e perfis
- Arquivos `application.yml` por serviço em `src/main/resources`.
- Perfis de teste costumam usar `application-test.yml`.
- Variáveis úteis:
  - `server.port` para trocar portas rapidamente.
  - Para desabilitar DB em Identity: `spring.autoconfigure.exclude` (veja seção "sem Docker").

### Roadmap
- Gateway: rate limit por tenant (Redis) e canary release.
- Identity: `authorization_code`, RBAC usuários in-memory, MFA TOTP básico, JWKS persistente.
- Orders: RFC7807 extra, ETag/If-None-Match, paginação por cursor.
- Persistência/mensageria: Postgres (schemas com RLS footprint), Kafka (Outbox/Sagas), Redis, OpenSearch; testes com Testcontainers.
- Observabilidade: Exporter OTel/Collector e métricas; smoke tests de métricas.
- Performance: scripts k6.
- Contratos: OpenAPI por serviço e contract tests.
- CI/CD: GitHub Actions; caos engineering básico.

### Troubleshooting
- Aviso Maven sobre `<version>` do plugin Spring Boot: o build funciona, mas é recomendável definir versões no `pom`.
- `Connection refused` para Postgres/Kafka/Redis: verifique containers/portas, usuários e senhas.
- Para rodar Identity sem DB local, use a flag de exclusão de autoconfig descrita acima.

