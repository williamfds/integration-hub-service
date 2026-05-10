# ADR-003 — Resposta do webhook: 202 Accepted

## Context
O endpoint `POST /webhooks/shopify/orders` recebe um payload, normaliza
para o modelo canônico e persiste no banco. Hoje o processamento é
síncrono dentro da própria request. Temos duas escolhas razoáveis para o
status code de sucesso:

- **201 Created**: indica que um recurso foi criado e está disponível.
- **202 Accepted**: indica que a requisição foi aceita para processamento,
  sem garantir que toda a lógica de pós-processamento já terminou.

## Decision
Retornamos **202 Accepted**.

Razões:
- Semanticamente, um webhook é um *evento* sendo entregue ao serviço — não
  uma criação direta de recurso pelo cliente. O cliente (Shopify) não está
  pedindo "criem essa order"; está notificando "essa order existe lá".
- Na Semana 2 o handler vai virar produtor de fila (idempotência via Redis,
  retry com Resilience4j, possivelmente desacoplar persistência). Sair
  de 201 para 202 depois quebraria contrato. Já entregamos com a
  semântica final.
- Plataformas como Shopify consideram qualquer 2xx como entrega bem
  sucedida — não há perda funcional.

## Consequences
- O corpo da resposta ainda traz o `OrderResponse` para facilitar testes e
  debug, mesmo sendo um 202 — convenção que vamos manter consistente em
  todos os webhooks.
- Clientes que se apoiem estritamente em 201 para confirmar "recurso
  criado" precisariam de outro mecanismo (GET no recurso). Aceitável: os
  consumidores reais são plataformas externas, não clientes internos.
