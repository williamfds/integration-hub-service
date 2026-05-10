# Integration Hub Service 🛒

Recebe webhooks de plataformas de e-commerce, normaliza o payload para um
modelo canônico interno e expõe uma API REST para consulta. A Semana 1
cobre apenas o conector da **Shopify** e o domínio de **pedidos**.

## Por que normalizar

Cada plataforma (Shopify, Nuvemshop, Mercado Livre) tem seu próprio
formato de pedido, nomes de campo, enums e regras de status. Qualquer
sistema downstream que precise reagir a "pedido pago" não deveria conhecer
esses detalhes. O hub é a fronteira que traduz vocabulário externo para um
modelo interno estável.

## Arquitetura

```
┌──────────────┐    POST /webhooks/shopify/orders     ┌────────────────────┐
│   Shopify    │ ───────────────────────────────────► │  Webhook adapter   │
└──────────────┘                                      │  (infra/web)       │
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

Hexagonal pragmática: o domínio só conhece a porta `OrderRepository`. JPA
fica isolada em `infrastructure/persistence` e nunca aparece em `domain/`
ou `application/`.

## Como rodar localmente

1. `docker compose up -d db` — sobe o PostgreSQL 16.
2. `./mvnw spring-boot:run` — sobe a aplicação em `http://localhost:8080`.
3. Swagger UI em `http://localhost:8080/swagger-ui.html`.

Para subir tudo via Docker: `docker compose up --build`.

## Testar o webhook

```bash
curl -X POST http://localhost:8080/webhooks/shopify/orders \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Id: demo-1" \
  -d @docs/examples/shopify-order-created.json
```

Resposta esperada: `202 Accepted` com o pedido canônico no corpo.
Em seguida:

```bash
curl http://localhost:8080/orders?platform=SHOPIFY
```

## Testes

```bash
./mvnw test
```

Os testes de integração usam **Testcontainers** (Postgres real). Não há
banco em memória — o objetivo é validar SQL e Flyway reais.

## Limitações conhecidas da Semana 1

- **Sem validação HMAC** do header `X-Shopify-Hmac-Sha256`. Qualquer
  cliente que conheça a URL pode publicar. O TODO está marcado no
  controller.
- **Sem idempotência real**. O header `X-Webhook-Id` é apenas logado.
  Reentregas da Shopify resultam em update de status — não em duplicata
  graças à constraint `(platform, external_id)`, mas isso não substitui
  idempotência de eventos.
- **Sem retry/circuit breaker** no caminho de persistência.

## Próximos passos (Semana 2)

- Idempotência baseada em `X-Webhook-Id` com Redis.
- Validação HMAC do webhook Shopify.
- Resilience4j (retry + circuit breaker) no caminho de saída.
- Conector Nuvemshop reusando o mesmo modelo canônico.
