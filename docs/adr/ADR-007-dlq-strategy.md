# ADR-007 — DLQ por exchange dedicada e plano de replay

## Context
Mensagens que esgotam retry ou são marcadas como não-recuperáveis precisam
ir para algum lugar inspecionável — para que humanos investiguem, corrijam
o consumer ou reprocessem manualmente. Sem DLQ, ou perdemos a mensagem ou
ela ocupa a fila principal indefinidamente.

## Decision
Topology:

- **Exchange principal:** `integration.events` (topic).
- **DLX:** `integration.events.dlx` (fanout).
- **Fila principal:** `shopify.order.webhook.received` com
  `x-dead-letter-exchange=integration.events.dlx`.
- **DLQ:** `shopify.order.webhook.received.dlq` ligada à DLX por binding
  fanout (sem routing-key — qualquer mensagem rejeitada cai aqui).

Uma mensagem só chega na DLQ quando:
- O consumer joga `AmqpRejectAndDontRequeueException` (caso
  `NonRecoverableException`, payload malformado), ou
- O retry esgota e o recoverer
  (`RejectAndDontRequeueRecoverer`) rejeita.

## Consequences
- Operacionalmente fica fácil: `rabbitmqctl list_queues messages_ready`
  e a UI em :15672 mostram a DLQ. Mensagens trazem header
  `x-death` com a razão (`rejected`, count, exchange/queue original).
- A topology é dedicada (uma DLX, uma DLQ) e não compartilhada com
  outras plataformas — quando vier Nuvemshop teremos a opção de criar
  outra DLQ específica ou reaproveitar a DLX fanout.
- O **replay** não está implementado. O plano para a Semana 3:
  ferramenta CLI/endpoint que consome da DLQ, re-publica na exchange
  principal e mantém um contador de tentativas. Hoje, mensagens na DLQ
  precisam ser reinjetadas manualmente via Management UI.
- Sem outbox ainda, é possível que uma mensagem "claime" idempotência no
  Redis mas falhe antes do publish. Esse caso volta como duplicata na
  reentrega da plataforma e é descartado. Ruim para auditoria; aceito
  por enquanto. // TODO (Semana 3): outbox pattern.
