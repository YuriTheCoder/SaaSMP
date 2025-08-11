## Marketplace SaaS – Arquitetura (Resumo)

- Microservices + monólito modular opcional para v1.
- Multi-tenancy real via header `X-Tenant-Id` e filtros.
- Segurança: OAuth2/OIDC, JWT RS256, MFA TOTP, RBAC.
- Resiliência: Outbox + Sagas, Circuit Breaker, Retry, Idempotência.
- Observabilidade: OTel tracing, Micrometer/Prometheus, logs JSON.

Veja `docs/openapi/payments.yaml` para exemplo OpenAPI 3.1.


