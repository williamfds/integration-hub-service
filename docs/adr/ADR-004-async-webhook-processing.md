# ADR-004 — Processamento assíncrono do webhook

## Context
Na Semana 1, `POST /webhooks/shopify/orders` recebia o payload, normalizava
e persistia tudo dentro da mesma request, devolvendo o pedido canônico no
corpo do 202. Esse modelo tem dois problemas reais quando o volume cresce:

- a latência percebida pela Shopify depende de Postgres + JPA + lógica de
  normalização. Qualquer lentidão (lock, GC, índice ruim) vira webhook
  expirado e replay.
- o pipeline de tratamento (idempotência, retry, observabilidade) fica
  todo dentro do controller, misturando responsabilidades.

## Decision
Quebrar o fluxo em dois estágios:

1. Controller faz só validação, rate limit, idempotência e publica no
   RabbitMQ. Devolve **202 Accepted** com `{webhookId, status}`.
2. Consumer (`ShopifyOrderWebhookListener`) faz a normalização e a
   persistência. Retry e DLQ vivem na fronteira de mensageria.

Breaking change: o webhook **não devolve mais o pedido canônico** no corpo.
O cliente que precisar do pedido consulta `GET /orders/{id}` depois.

## Consequences
- A request para a Shopify fica praticamente instantânea (validação +
  Redis SET NX + publish AMQP).
- Falhas transitórias do banco não derrubam o webhook: o consumer
  reprocessa.
- Surge uma janela curta entre 202 e a existência do pedido no banco.
  Isso é compatível com o modelo "entrega ao menos uma vez" — tratado
  pelos ADRs 005 e 007.
- Testes da Semana 1 que dependiam do pedido no corpo precisaram ser
  reescritos para usar `await()` + lookup por `externalId`.
