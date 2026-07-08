# SRM Credit Engine

Plataforma multimoeda de cessao de credito, simulacao de precificacao e liquidacao auditavel de recebiveis, desenvolvida como teste tecnico para vaga de Engenheiro de Software Pleno.

## 1. Visao Geral

O **SRM Credit Engine** e uma aplicacao para simulacao, precificacao e liquidacao de recebiveis em ambiente multimoeda.

O sistema recebe recebiveis de empresas cedentes, calcula valor presente com base em taxa base, prazo e spread de risco por tipo de ativo, aplica conversao cambial quando necessario e registra a liquidacao de forma auditavel.

O projeto foi especificado com foco em:

* precisao decimal para calculos financeiros;
* transacoes ACID;
* rastreabilidade das decisoes de calculo;
* arquitetura em camadas;
* uso explicito de Strategy Pattern nas regras de precificacao;
* API REST documentada com OpenAPI/Swagger;
* frontend SPA para operacao e consulta;
* fluxo Git organizado;
* uso controlado de IA como ferramenta de apoio.

## 2. Status do Projeto

Este repositorio esta na fase inicial de **Specification-Driven Development**.

Neste estagio, o objetivo e consolidar:

* especificacoes funcionais e tecnicas;
* decisoes arquiteturais;
* criterios de aceite;
* modelo de dados;
* diagramas;
* prompts de apoio para desenvolvimento assistido por IA;
* estrategia de implementacao incremental.

A implementacao sera realizada em branches de feature a partir da branch `develop`.

## 3. Contexto do Desafio

O sistema simula uma plataforma de cessao de credito para uma operacao financeira envolvendo recebiveis, como duplicatas mercantis e cheques pre-datados.

A aplicacao deve permitir:

* gestao de taxas de cambio;
* simulacao de precificacao;
* liquidacao de lotes de recebiveis;
* consulta analitica de liquidacoes;
* visualizacao operacional via frontend;
* documentacao tecnica suficiente para avaliacao do projeto.

## 4. Stack Tecnica Definida

### Backend

| Camada               | Tecnologia                       |
| -------------------- | -------------------------------- |
| Linguagem            | Java 21                          |
| Framework            | Spring Boot 4.1.x                |
| Build                | Maven                            |
| Banco de dados       | MySQL 8.4 LTS                    |
| Migracoes            | Flyway                           |
| Persistencia         | Spring Data JPA / Hibernate      |
| Documentacao de API  | OpenAPI / Swagger                |
| Testes               | JUnit 5, Mockito, AssertJ        |
| Testes de integracao | Testcontainers, quando aplicavel |

### Frontend

| Camada           | Tecnologia                                |
| ---------------- | ----------------------------------------- |
| Framework        | Angular 22                                |
| Linguagem        | TypeScript                                |
| UI               | Angular Material                          |
| Estado           | Signals, services e Reactive Forms        |
| Comunicacao HTTP | Angular HttpClient                        |
| Testes           | Ferramentas padrao do ecossistema Angular |

### Infraestrutura Local

| Recurso            | Tecnologia                            |
| ------------------ | ------------------------------------- |
| Orquestracao local | Docker Compose                        |
| Banco local        | MySQL                                 |
| Backend local      | Container ou execucao via Maven       |
| Frontend local     | Container ou execucao via Angular CLI |

## 5. Premissas de Negocio

As principais premissas iniciais do projeto sao:

1. O sistema suporta inicialmente as moedas `BRL` e `USD`.
2. Os tipos de recebivel iniciais sao `MERCANTILE_DUPLICATE` e `POST_DATED_CHECK`.
3. O spread mensal inicial por tipo de recebivel e:
   * Duplicata Mercantil: `1.5% a.m.`
   * Cheque Pre-datado: `2.5% a.m.`
4. A formula base de calculo e:

```text
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

5. A conversao cambial, quando necessaria, deve ser aplicada apos o calculo do valor presente na moeda de origem.
6. A taxa de cambio usada na liquidacao deve ser salva como snapshot auditavel.
7. Uma liquidacao em lote deve ser atomica: ou todos os itens sao liquidados, ou nenhum e persistido.
8. Valores monetarios, taxas e resultados financeiros nao devem ser calculados com tipos de ponto flutuante.
9. Todos os recebiveis do mesmo lote de liquidacao devem compartilhar uma unica `sourceCurrency`.
10. O `sourceCurrency` do lote deve constar no cabecalho da resposta de liquidacao.
11. O `baseRate` efetivo usado pelo backend deve seguir a ordem:
    * `baseRate` informado na requisicao;
    * fallback server-side `DEFAULT_BASE_RATE`;
    * falha estruturada se nenhum dos dois existir.
12. Quando uma operacao cross-currency exigir exchange rate e o par exato nao existir, a operacao deve falhar sem persistencia parcial.

## 6. Arquitetura Planejada

O backend segue arquitetura em camadas:

```text
api
 `-- controllers, DTOs, validation, OpenAPI annotations, exception handling

application
 `-- use cases, orchestration, transaction boundaries

domain
 `-- entities, value objects, business rules, pricing strategies

infrastructure
 `-- persistence, repositories, database queries, integrations
```

O frontend segue separacao entre estrutura de aplicacao, API access e features:

```text
frontend/
`-- src/
    `-- app/
        |-- core/
        |-- shared/
        |-- features/
        `-- app.config.ts
```

Regras arquiteturais importantes:

* a regra oficial de calculo financeiro fica no backend;
* o frontend pode solicitar simulacoes, mas nao e a fonte oficial de calculo de liquidacao;
* leituras simplificadas de relatorio nao devem pular a camada de aplicacao;
* persistencia e consultas nao devem carregar regra financeira para controllers.

## 7. Organizacao do Repositorio

```text
srm-credit-engine/
|-- AGENTS.md
|-- AI_USAGE.md
|-- README.md
|-- docker-compose.yml
|-- .gitignore
|-- .editorconfig
|-- .env.example
|-- docs/
|   |-- specs/
|   |-- diagrams/
|   |-- adr/
|   `-- prompts/
|-- backend/
`-- frontend/
```

## 8. Documentacao Tecnica

A documentacao principal do projeto esta organizada em:

### Specifications

```text
docs/specs/
|-- 00-spec-index.md
|-- 01-product-brief.md
|-- 02-domain-glossary.md
|-- 03-business-rules.md
|-- 04-api-contract.md
|-- 05-data-model.md
|-- 06-architecture.md
|-- 07-testing-strategy.md
|-- 08-acceptance-criteria.md
|-- 09-git-workflow.md
|-- 10-ai-workflow.md
`-- 11-delivery-checklist.md
```

### Architecture Decision Records

```text
docs/adr/
|-- ADR-001-backend-stack.md
|-- ADR-002-database-choice.md
|-- ADR-003-money-precision.md
|-- ADR-004-architecture-style.md
|-- ADR-005-frontend-stack.md
|-- ADR-006-git-workflow.md
`-- ADR-007-ai-assisted-development.md
```

### Diagrams

```text
docs/diagrams/
|-- er-diagram.md
|-- c4-context.md
`-- c4-container.md
```

### Development Prompts

```text
docs/prompts/
|-- 00-bootstrap.md
|-- 01-backend-scaffold.md
|-- 02-database-migrations.md
|-- 03-currency-engine.md
|-- 04-pricing-engine.md
|-- 05-settlement-flow.md
|-- 06-settlement-detail.md
|-- 07-statement-query.md
|-- 08-frontend-scaffold.md
|-- 09-frontend-simulation.md
|-- 10-frontend-statement-grid.md
|-- 11-frontend-exchange-rates.md
|-- 12-docker-and-delivery.md
`-- 13-review.md
```

## 9. Fluxo de Desenvolvimento

O projeto usa um fluxo inspirado em Git Flow simplificado:

```text
main
`-- develop
    |-- feature/backend-scaffold
    |-- feature/database-migrations
    |-- feature/currency-engine
    |-- feature/pricing-engine
    |-- feature/settlement-flow
    |-- feature/settlement-detail
    |-- feature/statement-query
    |-- feature/frontend-scaffold
    |-- feature/frontend-simulation
    |-- feature/frontend-statement-grid
    `-- feature/frontend-exchange-rates
```

### Branches

* `main`: branch estavel de entrega.
* `develop`: branch de integracao.
* `feature/*`: branches isoladas para cada incremento funcional.
* `docs/*`: branches para documentacao quando necessario.

### Pull Requests

Mesmo sendo um projeto individual, as features serao integradas por Pull Requests simulados para demonstrar organizacao, rastreabilidade e controle de historico.

### Commits

Os commits seguirao o padrao Conventional Commits:

```text
feat: add pricing strategy engine
fix: correct cross-currency rounding
test: cover settlement rollback scenario
docs: update architecture decision records
refactor: isolate pricing calculation service
chore: configure project tooling
```

## 10. Estrategia de Implementacao

A implementacao sera feita em etapas incrementais:

1. Fundacao do projeto e documentacao.
2. Scaffold do backend.
3. Migracoes e modelo de dados.
4. Currency Engine.
5. Pricing Engine com Strategy Pattern.
6. Fluxo de liquidacao atomica.
7. Consulta de detalhe de liquidacao persistida.
8. Consulta analitica de liquidacoes.
9. Scaffold do frontend.
10. Tela de simulacao.
11. Grid de transacoes com filtros e paginacao server-side.
12. Tela de cadastro de exchange rates.
13. Fechamento de Docker e entrega final.
14. Testes, revisao e documentacao final.

## 11. Criterios Tecnicos Prioritarios

Durante o desenvolvimento, as seguintes decisoes terao prioridade:

* correcao da regra financeira acima de conveniencia de implementacao;
* uso de `BigDecimal` para dinheiro, taxas e calculos financeiros;
* persistencia auditavel dos dados usados no calculo;
* uso de transacoes para liquidacao;
* validacao robusta de entrada;
* tratamento global de excecoes;
* queries otimizadas para relatorios;
* testes unitarios para regras de precificacao;
* documentacao clara de decisoes e trade-offs.

## 12. Uso de IA

O uso de IA e permitido neste projeto como ferramenta de apoio para:

* organizacao de especificacoes;
* scaffolding;
* geracao de casos de teste;
* revisao de codigo;
* refatoracao;
* documentacao;
* identificacao de riscos.

Todo uso relevante sera registrado no arquivo:

```text
AI_USAGE.md
```

A responsabilidade pelas decisoes, pelo codigo entregue e pela validacao das regras de negocio permanece integralmente com o autor do projeto.

## 13. Como Executar

A execucao local completa sera documentada apos a implementacao dos modulos backend e frontend.

A expectativa final e permitir execucao com:

```bash
docker compose up --build
```

Tambem deve haver suporte a execucao separada de backend e frontend para desenvolvimento local.

## 14. Entrega Final Planejada

A entrega final sera composta por:

* backend funcional;
* frontend funcional;
* banco MySQL versionado com Flyway;
* Docker Compose;
* documentacao OpenAPI/Swagger;
* testes unitarios;
* README final de setup e arquitetura;
* documentacao de uso de IA;
* diagrama ER;
* diagramas C4;
* historico Git organizado;
* Pull Requests simulados;
* merge final de `develop` para `main`;
* tag de release `v1.0.0`.

## 15. Status Atual

```text
Fase atual: Specification phase
Branch atual: develop
Proximo passo: consolidar specs, ADRs, diagramas e prompts antes da implementacao
```
