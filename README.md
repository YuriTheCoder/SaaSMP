## Marketplace SaaS (Multi-tenant)

Reference architecture for a multi-tenant marketplace built as a Maven monorepo. Services include identity, API gateway, orders, payments, and delivery, plus common platform components.

### Stack overview
- Java 17, Spring Boot 3
- Spring Authorization Server, Spring Cloud Gateway
- Kafka, Postgres, Redis (optional for demo flows here)
- React + Vite frontend

### Repository layout (main modules)
- `packages/identity` (8081): OAuth2 Authorization Server
- `packages/api-gateway` (8090): API Gateway/routing (+ optional tenant rate-limit)
- `packages/orders` (8082)
- `packages/payments` (8083)
- `packages/delivery-dispatch` (8084)
- `platform/common`: shared filters, security, web utilities (auto-config)
- `apps/web`: React frontend (Vite)

### Quickstart
1) Infra (optional for the demo flows):
```
docker run -d --name pg -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=marketplace postgres:16
docker run -d --name redis -p 6379:6379 redis:7
```
2) Build common platform once:
```
mvn -q -DskipTests install -pl platform/common -am
```
3) Run services (separate terminals):
```
mvn -q -pl packages/api-gateway spring-boot:run           # 8090
mvn -q -pl packages/orders spring-boot:run                # 8082
mvn -q -pl packages/payments spring-boot:run              # 8083
mvn -q -pl packages/delivery-dispatch spring-boot:run     # 8084
# Optional OAuth2 server:
mvn -q -pl packages/identity spring-boot:run              # 8081
```
Health checks:
```
curl http://localhost:8090/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

### Frontend (Vite)
- Located in `apps/web`. Proxies `/v1/*` to the gateway at `http://localhost:8090` in dev.
- Sends the required `X-Tenant-Id` header on all requests; set tenant in the top-right input.

Dev mode:
```
cd apps/web
npm install
npm run dev
# http://localhost:5173
```

Docker:
```
docker compose up -d web
# http://localhost:5173
```
Note: If `host.docker.internal` is not available on Linux, change `apps/web/nginx.conf` to use your host IP (e.g., `http://172.17.0.1:8090`).

### End-to-end manual test (UI)
1) Open `http://localhost:5173`.
2) Set a tenant (e.g., `demo-tenant`).
3) Flow: Home → Create Cart → Add Item(s) → Checkout → Create Payment Intent → Confirm → Refund (Payments page) → Dispatch Delivery (Orders page) → Track (Delivery page).

### Manual test via cURL (through the gateway @ 8090)
```
TENANT=demo-tenant
curl -i -X POST http://localhost:8090/v1/carts -H "X-Tenant-Id: $TENANT"
curl -i -X POST http://localhost:8090/v1/carts/CART_ID/items -H "Content-Type: application/json" -H "X-Tenant-Id: $TENANT" -d '{"sku":"SKU-1","qty":2}'
curl -i -X POST http://localhost:8090/v1/carts/CART_ID/checkout -H "Content-Type: application/json" -H "X-Tenant-Id: $TENANT" -d '{}'
curl -i -X POST http://localhost:8090/v1/payment-intents -H "Content-Type: application/json" -H "X-Tenant-Id: $TENANT" -H "Idempotency-Key: $(uuidgen 2>/dev/null || powershell -Command "[guid]::NewGuid().ToString()")" -d '{"amount":1000,"currency":"BRL","method":"card","orderId":"ORDER_ID"}'
curl -i -X POST http://localhost:8090/v1/payment-intents/PI_ID/confirm -H "X-Tenant-Id: $TENANT"
curl -i -X POST http://localhost:8090/v1/refunds -H "Content-Type: application/json" -H "X-Tenant-Id: $TENANT" -H "Idempotency-Key: $(uuidgen 2>/dev/null || powershell -Command "[guid]::NewGuid().ToString()")" -d '{"payment_intent":"PI_ID","amount":500}'
curl -i -X POST http://localhost:8090/v1/deliveries/dispatch -H "Content-Type: application/json" -H "X-Tenant-Id: $TENANT" -d '{"orderId":"ORDER_ID"}'
curl -s http://localhost:8090/v1/deliveries/DELIVERY_ID/track -H "X-Tenant-Id: $TENANT"
```

### Automated tests
- Backend:
```
mvn -q -DskipTests=false test
```
- Frontend:
```
cd apps/web
npm run test
npm run test:coverage
```

### OAuth2 (optional)
```
curl -u test-client:test-secret \
  -d "grant_type=client_credentials&scope=payments:write" \
  http://localhost:8081/oauth2/token
```

### Troubleshooting
- 400 “Invalid tenant header”: tenant must match `[a-zA-Z0-9_-]{3,64}`.
- Frontend container cannot reach gateway: edit `apps/web/nginx.conf` target host/port.
- DB/Kafka connectivity: ensure containers/ports are reachable.
