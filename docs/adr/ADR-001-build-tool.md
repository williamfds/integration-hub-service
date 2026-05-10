# ADR-001 — Build tool: Maven

## Context
O projeto é um serviço Spring Boot 3.4.x em Java 21, pensado como portfólio
público. Precisamos de uma ferramenta de build que: (a) seja familiar para a
maioria dos leitores do código, (b) tenha boa integração com o ecossistema
Spring, (c) não introduza fricção de configuração inicial.

As opções consideradas foram Maven e Gradle (Kotlin DSL).

## Decision
Adotamos **Maven**.

Razões principais:
- O ecossistema Spring Boot continua tendo Maven como caminho de menor
  resistência: o starter parent já resolve versões transitivas sem boilerplate.
- `pom.xml` é declarativo e legível por inspeção. Reviewers de portfólio
  conseguem entender as dependências sem precisar ler DSL.
- Não há build customizado complexo que justifique a flexibilidade do Gradle.
- Tempo de build é aceitável para o tamanho do projeto; perdas em
  performance não compensam o custo cognitivo.

## Consequences
- Build incremental é menos rápido que Gradle, mas irrelevante na escala atual.
- Caso o projeto cresça para multi-módulo com lógica de build não-trivial,
  reabriremos a decisão.
- Plugins customizados (geração de código, análise) ficam mais verbosos —
  aceitável dado o escopo.
