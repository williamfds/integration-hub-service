# ADR-006 — Retry na fronteira de mensageria

## Context
Falhas transitórias acontecem (Postgres com lock momentâneo, blip de
rede). Sem retry, o consumer perde mensagens — e como o `ack` é manual,
mensagens sem retry voltam pra fila indefinidamente, ocupando recursos
sem progredir.

Três alternativas consideradas:
- `@Retryable` do Spring Retry diretamente no use case.
- Retry "manual" dentro do listener (try/catch + loop com sleep).
- `RetryInterceptor` do Spring AMQP no `SimpleRabbitListenerContainerFactory`.

## Decision
Retry vive **no listener container factory**, configurado via
`RetryInterceptorBuilder.stateless()` com:

- `SimpleRetryPolicy` (max-attempts configurável, default 5).
- Backoff exponencial (initial 1s, multiplier 2, max 30s).
- `RejectAndDontRequeueRecoverer` após esgotamento — joga na DLX.

## Consequences
- O use case (`RegisterIncomingOrder`) segue ignorante sobre retry. Ele
  só faz o trabalho de domínio.
- Não usamos `@Retryable` em service, evitando interação ruim com a
  transação JPA do listener (a transação reabriria a cada tentativa,
  poluindo logs e métricas).
- Mensagens com erro definitivo (payload malformado, NumberFormatException
  no `total_price`) viram `NonRecoverableException` no listener, que
  o próprio listener converte em `AmqpRejectAndDontRequeueException`.
  O Rabbit reconhece essa exception e roteia direto pra DLX sem retry.
- A `SimpleRetryPolicy` está configurada com `defaultValue=true` e
  `traverseCauses=true`, mapeando `AmqpRejectAndDontRequeueException →
  false`. Isso é necessário porque o Spring AMQP embrulha exceptions do
  listener em `ListenerExecutionFailedException`; só percorrendo causes
  conseguimos detectar a exception "definitiva" embutida.
