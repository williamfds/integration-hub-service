# ADR-005 — Idempotência via Redis SET NX

## Context
Plataformas de e-commerce retentam webhooks agressivamente quando o
endpoint demora a responder ou retorna erro. O serviço precisa garantir
que um mesmo evento gere no máximo um pedido persistido — mesmo sob
reentregas rápidas e concorrentes.

Duas estratégias razoáveis:
- **Postgres unique constraint** em `(platform, external_id)` (já temos)
  como única defesa.
- **Redis com SET NX e TTL** como dedup explícita por `X-Webhook-Id`,
  antes de qualquer trabalho no consumer.

## Decision
Usar Redis (`SET NX` com TTL de 24h) para idempotência **na entrada da
API**, indexada pelo `X-Webhook-Id`. A unique constraint do Postgres fica
como segunda linha de defesa (caso o evento traga o mesmo `external_id`
com `webhookId` diferentes).

A chave é `webhook:shopify:order:{webhookId}` e armazena um JSON com
status (`RECEIVED → PUBLISHED → PROCESSED`) para auditoria curta.

## Consequences
- Reentregas próximas no tempo (janela de 24h) são descartadas no
  controller — não chegam a publicar mensagem nem consultar banco.
- Webhook duplicado responde **202** com `{"status": "duplicate"}`. 202
  porque o webhook foi aceito (ack para a Shopify), apenas não gerou
  trabalho novo. 200 sugeriria "processei agora", o que é falso; 409
  faria a Shopify retentar.
- Redis indisponível derruba o fluxo principal. Trade-off aceito:
  preferimos falhar a request a deixar passar duplicata silenciosa.
- A janela de 24h é suficiente para os SLAs típicos da Shopify; eventos
  fora dela são raros e a unique constraint do banco protege esses
  casos residuais.
- Domínio não conhece Redis. A interface `IdempotencyStore` vive em
  `infrastructure/idempotency/`, não em `domain/`.
