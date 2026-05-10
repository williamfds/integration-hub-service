# ADR-002 — Migration tool: Flyway

## Context
A persistência usa PostgreSQL 16 com schema versionado. Precisamos de uma
ferramenta de migração que: (a) deixe o histórico de schema legível no
repositório, (b) integre nativamente com Spring Boot, (c) não exija
tradução mental entre formatos.

Opções consideradas: Flyway e Liquibase.

## Decision
Adotamos **Flyway** com migrations em SQL puro (`V1__create_orders_table.sql`).

Razões:
- SQL puro é a linguagem que o banco entende. Quem revisa a migration vê
  exatamente o DDL que vai rodar — sem abstração intermediária.
- Spring Boot integra Flyway com configuração mínima (basta a dependência e
  a pasta `db/migration`).
- Para o escopo atual (uma tabela, sem rollback automático complexo) o
  modelo linear baseado em versão (`V1`, `V2`, ...) é suficiente.

## Consequences
- Não temos rollback declarativo "grátis" como em Liquibase. Para reverter
  uma migration precisamos escrever a operação inversa como nova versão —
  que é, na prática, a recomendação para produção mesmo em Liquibase.
- Se no futuro precisarmos suportar múltiplos dialetos SQL ou mudar de
  banco com frequência, reabriremos a decisão (Liquibase abstrai dialeto
  via XML/YAML).
- Migrations específicas de Postgres (CTEs, JSONB, índices parciais) podem
  ser escritas sem fricção.
