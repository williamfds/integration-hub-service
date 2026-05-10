# Integration Hub Service

[![CI](https://github.com/williamfds/integration-hub-service/actions/workflows/ci.yml/badge.svg)](https://github.com/williamfds/integration-hub-service/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.4](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)

Recebe webhooks de plataformas de e-commerce, normaliza para um modelo
canônico interno e expõe uma API REST de pedidos. Semana 1 cobre a Shopify.

## Por que normalizar

Cada plataforma (Shopify, Nuvemshop, Mercado Livre) tem seu próprio
formato, nomes de campo, enums e regras de status. Sistemas downstream
que reagem a "pedido pago" não deveriam conhecer esses detalhes. O hub
é a fronteira que traduz o vocabulário externo para um modelo interno
estável.

## Arquitetura

```
┌──────────────┐   POST /webhooks/shopify/orders   ┌────────────────────┐
│   Shopify    │ ─────────────────────────────────►│  Webhook adapter   │
└──────────────┘                                   │  (infra/web)       │
                                                   └─────────┬──────────┘
                                                             │ ShopifyOrderMapper
                                                             ▼
                                                   ┌────────────────────┐
                                                   │  Use case          │
                                                   │  RegisterIncoming  │
                                                   └─────────┬──────────┘
                                                             │ OrderRepository (porta)
                                                             ▼
                                                   ┌────────────────────┐
                                                   │  JPA Adapter       │
                                                   │  → PostgreSQL      │
                                                   └────────────────────┘
```

Hexagonal pragmática: o domínio só conhece a porta `OrderRepository`.
JPA fica isolada em `infrastructure/persistence` e não aparece em
`domain/` ou `application/`.

## Rodar localmente

1. `docker compose up -d db`
2. `./mvnw spring-boot:run`
3. Swagger UI em `http://localhost:8080/swagger-ui.html`

Para subir tudo via Docker: `docker compose up --build`.

## Testar

```bash
curl -X POST http://localhost:8080/webhooks/shopify/orders \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Id: demo-1" \
  -d @docs/examples/shopify-order-created.json

curl http://localhost:8080/orders?platform=SHOPIFY
```

`./mvnw verify` roda testes unitários e os de integração com
Testcontainers (Postgres real, sem H2). Requer Docker.

## Decisões registradas

- [ADR-001](docs/adr/ADR-001-build-tool.md) — Maven
- [ADR-002](docs/adr/ADR-002-migration-tool.md) — Flyway
- [ADR-003](docs/adr/ADR-003-webhook-response-code.md) — 202 Accepted

## Limitações da Semana 1

- Sem validação HMAC do header `X-Shopify-Hmac-Sha256`.
- Sem idempotência real (o header `X-Webhook-Id` é apenas logado).
- Sem retry/circuit breaker no caminho de saída.

## Próximos passos (Semana 2)

- Idempotência via `X-Webhook-Id` com Redis.
- Validação HMAC da Shopify.
- Resilience4j (retry + circuit breaker).
- Conector Nuvemshop reusando o mesmo modelo canônico.
