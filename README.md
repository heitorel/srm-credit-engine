# SRM Credit Engine

SRM Credit Engine e uma plataforma multimoeda para cadastro de exchange rates, simulacao de precificacao, liquidacao atomica de recebiveis e consulta auditavel de historico. O projeto foi desenvolvido como desafio tecnico para uma vaga de Software Engineer Pleno, com foco em precisao financeira, consistencia transacional e rastreabilidade.

## Overview

O sistema cobre:

* gestao manual de exchange rates BRL/USD;
* simulacao de pricing com Strategy Pattern por tipo de recebivel;
* liquidacao atomica de lotes com snapshot auditavel dos calculos;
* consulta paginada e filtravel de settlements;
* frontend Angular para operacao;
* API REST documentada via OpenAPI/Swagger.

## Business Rules

Regras centrais do dominio:

* calculos financeiros usam `BigDecimal`, nunca `double` ou `float`;
* o backend e a fonte oficial de calculo;
* a conversao cambial ocorre apos o calculo do valor presente na moeda de origem;
* batches de settlement devem ser atomicos;
* um receivable nao pode ser liquidado duas vezes;
* o settlement persiste snapshots auditaveis de inputs e outputs.

Formula base:

```text
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

## Stack

### Backend

* Java 21
* Spring Boot 4.1.0
* Maven Wrapper
* MySQL 8.4 LTS
* Flyway
* Spring Data JPA / Hibernate
* Springdoc OpenAPI
* JUnit 5 + Testcontainers

### Frontend

* Angular 22
* TypeScript
* Angular Material
* Reactive Forms
* Signals + services
* Nginx para entrega do build em container

### Local Runtime

* Docker Compose

## Architecture

O backend segue arquitetura em camadas:

```text
api
 `-- controllers, DTOs, validation, OpenAPI, exception handling

application
 `-- orchestration, transaction boundaries, use cases

domain
 `-- entities, value objects, pricing strategies, business rules

infrastructure
 `-- JPA entities, repositories, SQL queries, config, migrations
```

O frontend segue organizacao por features:

```text
frontend/src/app/
|-- core/
|-- shared/
|-- features/
`-- models/
```

Referencias principais:

* `docs/specs/06-architecture.md`
* `docs/adr/ADR-004-architecture-style.md`
* `docs/adr/ADR-005-frontend-stack.md`
* `docs/diagrams/er-diagram.md`
* `docs/diagrams/c4-context.md`
* `docs/diagrams/c4-container.md`

## Main Endpoints

* `GET /api/reference-data/currencies`
* `GET /api/reference-data/receivable-types`
* `POST /api/exchange-rates`
* `GET /api/exchange-rates/latest`
* `POST /api/pricing/simulations`
* `POST /api/settlements`
* `GET /api/settlements/{id}`
* `GET /api/settlements/statement`

## Prerequisites

Para o caminho principal de execucao:

* Docker Desktop ou Docker Engine com Compose
* Docker daemon em execucao

Para execucao fora de containers:

* Java 21
* Node.js 22
* npm 11

## Environment Variables

Copie `.env.example` para `.env` se quiser customizar a execucao local:

```bash
cp .env.example .env
```

Variaveis principais:

| Variable | Purpose | Default |
| --- | --- | --- |
| `BACKEND_PORT` | Porta publicada do backend | `8080` |
| `FRONTEND_PORT` | Porta publicada do frontend | `4200` |
| `MYSQL_PORT` | Porta publicada do MySQL | `3306` |
| `MYSQL_DATABASE` | Nome do schema MySQL | `srm_credit_engine` |
| `SPRING_DATASOURCE_URL` | JDBC usado pelo backend em container | `jdbc:mysql://mysql:3306/srm_credit_engine?...` |
| `SPRING_DATASOURCE_USERNAME` | Usuario do datasource | `srm_app` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do datasource | `srm_app_password` |
| `CORS_ALLOWED_ORIGINS` | Origins permitidas pelo backend | `http://localhost:4200` |
| `ANGULAR_API_BASE_URL` | Base URL consumida pelo frontend | `http://localhost:8080/api` |
| `DEFAULT_BASE_RATE` | Fallback server-side de base rate | `0.01000000` |
| `SUPPORTED_CURRENCIES` | Codigos aceitos pelo backend | `BRL,USD` |
| `STARTUP_TEST_DATA_ENABLED` | Ativa a massa inicial controlada para validacao local | `true` no Docker Compose |

Observacoes:

* `DEFAULT_BASE_RATE` so atua como fallback server-side quando a requisicao nao envia `baseRate`.
* `ANGULAR_API_BASE_URL` e carregada por runtime config no container do frontend.
* `.env.example` contem apenas valores seguros de exemplo.
* `STARTUP_TEST_DATA_ENABLED` deve permanecer `false` em execucoes orientadas a producao.

## Running with Docker

Esse e o caminho principal e esperado para avaliacao:

```bash
docker compose up --build
```

A stack sobe:

* MySQL em `localhost:3306`
* backend em `http://localhost:8080`
* frontend em `http://localhost:4200`
* massa inicial controlada no banco para o frontend nao abrir vazio

URLs uteis:

* Frontend: `http://localhost:4200`
* Swagger UI: `http://localhost:8080/swagger-ui.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Startup Test Data

No caminho principal com Docker Compose, o backend sobe com uma massa inicial controlada para facilitar validacao manual logo no primeiro boot local.

O dataset inclui:

* exchange rates `BRL -> USD` e `USD -> BRL`, com historico suficiente para validar latest lookup;
* assignors de exemplo;
* receivables disponiveis para novos testes manuais;
* settlements auditaveis ja persistidos para popular a grid de historico e a tela de detalhe.

Exemplos incluidos:

* assignor `ACME Comercio Ltda.` (`12345678000199`)
* assignor `Orbit Foods S.A.` (`99887766000155`)
* assignor `Blue Export LLC` (`55667788990011`)
* receivable disponivel `LUM-AV-001`
* receivable disponivel `LUM-AV-002`

Para execucao local fora do Docker, habilite explicitamente a massa inicial quando quiser essa experiencia:

```powershell
$env:STARTUP_TEST_DATA_ENABLED='true'
cd backend
.\mvnw.cmd spring-boot:run
```

Se preferir subir sem massa inicial, defina:

```text
STARTUP_TEST_DATA_ENABLED=false
```

Para encerrar:

```bash
docker compose down
```

Para encerrar removendo o volume do banco:

```bash
docker compose down -v
```

## Running Locally Without Docker

### Backend

Com MySQL local disponivel e variaveis ajustadas:

```bash
cd backend
./mvnw spring-boot:run
```

No PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
```

O frontend aponta por padrao para:

```text
http://localhost:8080/api
```

## Database and Migrations

O schema e versionado com Flyway em:

```text
backend/src/main/resources/db/migration
```

Migrations atuais:

* `V1__create_initial_schema.sql`
* `V2__seed_reference_data.sql`

O backend usa `spring.jpa.hibernate.ddl-auto=validate`, entao o schema oficial vem das migrations, nao de auto-DDL do Hibernate.

## Frontend Scope

O frontend entrega:

* tela de pricing simulation;
* grid de settlement statement com filtros e paginacao server-side;
* tela de cadastro manual de exchange rates;
* tratamento de loading, empty state e backend errors.

O frontend nao implementa a formula oficial de pricing; ele consome os endpoints do backend.

## Validation Commands

Backend:

```bash
cd backend
./mvnw test
```

Frontend build:

```bash
cd frontend
npm run build
```

Frontend tests:

```bash
cd frontend
npm test -- --watch=false
```

Docker Compose:

```bash
docker compose config
docker compose up --build
```

## Documentation Map

* Specs: `docs/specs/`
* ADRs: `docs/adr/`
* Diagrams: `docs/diagrams/`
* AI prompts: `docs/prompts/`
* AI usage log: `AI_USAGE.md`
* Agent instructions: `AGENTS.md`

## Git Workflow

Fluxo esperado:

```text
main
`-- develop
    |-- feature/*
    |-- docs/*
    |-- fix/*
    `-- chore/*
```

Regras:

* feature branches saem de `develop`;
* commits seguem Conventional Commits;
* integracao acontece por PRs simulados;
* entrega final sera merge de `develop` em `main` com tag `v1.0.0`.

Referencias:

* `docs/specs/09-git-workflow.md`
* `docs/adr/ADR-006-git-workflow.md`

## AI Usage

O projeto usa IA como apoio controlado para planejamento, implementacao, testes, revisao e documentacao. Todo uso material fica registrado em `AI_USAGE.md`.

Referencias:

* `AI_USAGE.md`
* `docs/specs/10-ai-workflow.md`
* `docs/adr/ADR-007-ai-assisted-development.md`

## Known Limitations

* autenticacao e autorizacao estao fora de escopo;
* exchange rates sao cadastradas manualmente;
* nao ha integracao bancaria real;
* `DEFAULT_BASE_RATE` depende de configuracao;
* mixed-source-currency batches sao rejeitados em vez de suportados;
* nao ha pipeline CI/CD nem deploy cloud como requisito central;
* observabilidade avancada e integracoes externas ficaram fora do escopo inicial.

## Future Improvements

* autenticacao e autorizacao;
* modulo de gestao de base rate;
* integracao com provedor real de exchange rates;
* approval workflow para settlements;
* exportacao do statement;
* CI/CD automatizado;
* metricas, tracing e health checks mais completos;
* estrategias de escala para consultas historicas.
