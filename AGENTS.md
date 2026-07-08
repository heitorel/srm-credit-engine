# AGENTS.md

## 1. Project Context

This repository implements **SRM Credit Engine**, a technical challenge for a mid-level Software Engineer position.

The application is a multi-currency credit assignment platform responsible for:

* managing exchange rates;
* simulating receivable pricing;
* settling receivable batches;
* storing auditable settlement records;
* exposing REST APIs documented with OpenAPI/Swagger;
* providing an Angular frontend for operators.

The project follows **Specification-Driven Development**. Before implementing or changing code, read the relevant specification files under `docs/specs/` and the relevant Architecture Decision Records under `docs/adr/`.

## 2. Core Business Constraints

The system operates in a financial domain. Correctness, precision and auditability are more important than implementation convenience.

Mandatory business constraints:

* Monetary values must never be calculated with floating-point types.
* Financial calculations must use decimal-safe types.
* The backend is the official source of financial calculation.
* The frontend must not duplicate official settlement calculation logic.
* Exchange rates must not be hardcoded in pricing logic.
* Cross-currency conversion must be applied after present value calculation.
* Settlement batches must be atomic.
* A settlement must not be partially persisted.
* A receivable must not be settled twice.
* Calculation inputs used during settlement must be persisted as auditable snapshots.

## 3. Technology Stack

### Backend

Use:

* Java 21;
* Spring Boot 4.1.x;
* Maven;
* MySQL 8.4 LTS;
* Flyway;
* Spring Data JPA / Hibernate;
* OpenAPI / Swagger;
* JUnit 5;
* Mockito;
* AssertJ;
* Testcontainers when integration tests require database behavior.

### Frontend

Use:

* Angular 22;
* TypeScript;
* Angular Material;
* Angular Reactive Forms;
* Angular HttpClient;
* Angular Signals and services for state management.

Do not introduce NgRx unless a specification or ADR is updated to justify it.

## 4. Repository Structure

Expected high-level structure:

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

Do not move files or introduce new top-level directories unless there is a clear reason and the relevant documentation is updated.

## 5. Specification Reading Order

Before working on a feature, read the relevant files.

General reading order:

1. `docs/specs/00-spec-index.md`
2. `docs/specs/01-product-brief.md`
3. `docs/specs/02-domain-glossary.md`
4. `docs/specs/03-business-rules.md`
5. Relevant API, data, architecture or testing specs
6. Relevant ADRs

For backend changes, usually read:

* `docs/specs/03-business-rules.md`
* `docs/specs/04-api-contract.md`
* `docs/specs/05-data-model.md`
* `docs/specs/06-architecture.md`
* `docs/specs/07-testing-strategy.md`
* relevant ADRs under `docs/adr/`

For frontend changes, usually read:

* `docs/specs/04-api-contract.md`
* `docs/specs/06-architecture.md`
* `docs/specs/08-acceptance-criteria.md`
* `docs/adr/ADR-005-frontend-stack.md`

For AI workflow or prompt changes, read:

* `docs/specs/10-ai-workflow.md`
* `AI_USAGE.md`
* `docs/adr/ADR-007-ai-assisted-development.md`

## 6. Backend Architecture Rules

The backend must follow layered architecture.

Expected layers:

```text
api
 `-- controllers, DTOs, validation, OpenAPI annotations, exception mapping

application
 `-- use cases, orchestration, transaction boundaries

domain
 `-- entities, value objects, domain services, pricing strategies, business rules

infrastructure
 `-- persistence, repositories, database queries, migrations, integrations
```

### API Layer Rules

Allowed:

* REST controllers;
* request DTOs;
* response DTOs;
* validation annotations;
* OpenAPI annotations;
* exception mapping.

Forbidden:

* financial calculation logic;
* transaction orchestration;
* direct use of low-level persistence details;
* hardcoded business rules.

### Application Layer Rules

Allowed:

* use case orchestration;
* transaction boundaries;
* coordination between repositories and domain services;
* settlement workflow;
* mapping between domain results and application responses.

Expected:

* settlement use cases must be transactional;
* batch settlement must rollback completely when any item is invalid;
* application services must not hide business rules in procedural code when the domain layer is the correct place.

### Domain Layer Rules

Allowed:

* entities;
* value objects;
* domain services;
* Strategy Pattern implementations;
* domain exceptions;
* business rules.

Expected:

* pricing rules must be represented through Strategy Pattern;
* money, rates and currencies should be modeled explicitly;
* business rules must be testable without requiring a running web server.

### Infrastructure Layer Rules

Allowed:

* JPA entities and repositories;
* Flyway migrations;
* SQL queries;
* query builders or native queries for analytical reports;
* mock external integrations.

Expected:

* read/report queries may use optimized SQL and projections;
* migrations must be deterministic;
* database constraints must support business invariants where appropriate.

## 7. Frontend Architecture Rules

The frontend must separate presentation, state and API access.

Expected structure:

```text
frontend/
`-- src/
    `-- app/
        |-- core/
        |-- shared/
        |-- features/
        `-- app.config.ts
```

Recommended organization:

```text
features/
|-- pricing-simulation/
|-- settlements/
`-- exchange-rates/
```

Frontend rules:

* use Angular Reactive Forms for operator inputs;
* use Angular HttpClient for API communication;
* use services for API access;
* use Signals and component-level state where sufficient;
* do not duplicate official financial settlement logic in the frontend;
* use backend simulation endpoint for official preview values;
* use server-side pagination for transaction grids;
* keep components focused on UI behavior.

## 8. Financial Calculation Rules

Mandatory:

* use `BigDecimal` in Java for money, rates and calculation results;
* do not use `double`, `float`, `Double` or `Float` for financial values;
* define explicit scale and rounding behavior;
* preserve enough precision during intermediate calculations;
* round only at controlled boundaries;
* persist the calculation inputs used during settlement.

Pricing formula:

```text
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

Cross-currency rule:

```text
1. Calculate present value in the source currency.
2. Convert the result to payment currency.
3. Persist the exchange rate snapshot used.
```

Do not change these rules without updating:

* `docs/specs/03-business-rules.md`;
* `docs/specs/04-api-contract.md`;
* `docs/specs/05-data-model.md`;
* relevant ADRs;
* tests.

## 9. Database and Migration Rules

Use MySQL 8.4 LTS and Flyway.

Rules:

* schema changes must be versioned through Flyway migrations;
* do not rely only on ORM auto-DDL for the final project;
* monetary columns must use `DECIMAL`, not floating-point database types;
* important business invariants should be supported by constraints where possible;
* add indexes for analytical filters such as period, assignor and currency;
* migration names must be descriptive.

Example naming:

```text
V1__create_initial_schema.sql
V2__seed_reference_data.sql
V3__add_settlement_indexes.sql
```

## 10. API Rules

The API must be RESTful and documented through OpenAPI/Swagger.

Rules:

* use semantic HTTP methods;
* use semantic HTTP status codes;
* validate input payloads;
* return structured error responses;
* do not leak stack traces to API consumers;
* document request and response examples where useful.

Expected status behavior:

```text
200 OK                 successful read or simulation
201 Created            successful creation
400 Bad Request         malformed or invalid request
404 Not Found           resource not found
409 Conflict            business conflict, such as duplicate settlement
422 Unprocessable Entity valid payload but invalid business rule
500 Internal Server Error unexpected controlled failure
```

## 11. Error Handling Rules

The backend must include global exception handling.

Expected:

* validation errors are returned in structured format;
* domain errors are mapped to meaningful HTTP responses;
* unexpected errors are handled without exposing internal implementation details;
* logs preserve diagnostic value.

Do not allow unhandled exceptions to produce inconsistent API responses.

## 12. Testing Rules

Tests are mandatory for business-critical logic.

Minimum backend unit test coverage must include:

* Mercantile Duplicate pricing;
* Post-Dated Check pricing;
* spread strategy resolution;
* same-currency pricing;
* cross-currency conversion applied after present value calculation;
* missing exchange rate;
* invalid due date;
* invalid monetary values;
* rounding behavior.

Recommended integration tests:

* Flyway migration validation;
* exchange rate persistence and retrieval;
* atomic settlement rollback;
* duplicate settlement prevention;
* statement query filters.

When changing business logic, update or add tests in the same task.

## 13. Docker Rules

The final project must run locally with Docker Compose.

Expected services:

* MySQL database;
* backend application;
* frontend application.

The final setup should support:

```bash
docker compose up --build
```

Environment variables must be documented in `.env.example`.

Do not commit real secrets.

## 14. Git Rules

Use the project Git workflow defined in `docs/specs/09-git-workflow.md`.

Expected branch model:

```text
main
`-- develop
    `-- feature/*
```

Rules:

* do not work directly on `main`;
* create feature branches from `develop`;
* merge features back into `develop` through simulated Pull Requests;
* merge final `develop` into `main`;
* tag final delivery as `v1.0.0`;
* keep history readable.

Use Conventional Commits:

```text
feat: add currency exchange engine
fix: correct monetary rounding
test: cover pricing strategy cases
docs: update data model specification
refactor: isolate pricing domain service
chore: configure docker compose
```

Avoid:

* vague commit messages;
* unrelated changes in the same commit;
* large unreviewable diffs;
* noisy merge commits when avoidable.

## 15. AI-Assisted Development Rules

AI tools may be used as engineering support, but all output must be reviewed.

Allowed AI usage:

* scaffolding;
* test case generation;
* documentation support;
* refactoring suggestions;
* SQL query review;
* code review assistance;
* prompt-driven implementation of small scoped tasks.

Forbidden AI usage:

* accepting financial logic without review;
* accepting generated code with floating-point financial calculations;
* hiding assumptions;
* committing code that the author does not understand;
* skipping tests because code was generated by AI;
* using AI output as justification for insecure or incorrect behavior.

Every relevant use of AI must be summarized in `AI_USAGE.md`.

When completing an AI-assisted task, update or propose an update to `AI_USAGE.md` with:

* prompt summary;
* generated result;
* review performed;
* mistakes found;
* corrections applied.

## 16. Scope Control Rules

Keep each task small and scoped.

Before implementing, identify:

* which spec applies;
* which ADR applies;
* which files are allowed to change;
* which tests should be added or updated.

Do not implement unrelated features opportunistically.

Do not introduce advanced architecture components such as Kafka, Kubernetes, distributed tracing or NgRx unless the specs and ADRs are explicitly updated to justify the decision.

## 17. Security and Validation Rules

Input validation is mandatory.

Validate at least:

* required fields;
* positive monetary values;
* valid currency codes;
* valid receivable types;
* future due dates;
* positive exchange rates;
* pagination limits;
* date range consistency;
* batch size limits.

Do not trust frontend validation alone.

## 18. Reporting and Analytical Query Rules

Settlement statement queries must support filtering by:

* period;
* assignor;
* currency;
* receivable type;
* pagination.

For reporting, optimized SQL or query builders are allowed and preferred over loading large datasets into memory.

Do not implement reports by fetching all records and filtering in application memory.

## 19. Documentation Rules

When changing architecture, business rules, APIs or data model, update the relevant documentation.

Potentially impacted files:

* `README.md`;
* `AI_USAGE.md`;
* `docs/specs/*`;
* `docs/adr/*`;
* `docs/diagrams/*`;
* `docs/prompts/*`.

Documentation should explain decisions, not only describe files.

## 20. Completion Checklist for Any Task

Before considering a task complete, verify:

* relevant specs were followed;
* relevant ADRs were followed;
* business rules were not bypassed;
* financial calculations do not use floating-point types;
* tests were added or updated when needed;
* relevant tests pass;
* documentation was updated when needed;
* the diff is scoped and understandable;
* no secrets or local-only files were committed.

## 21. Expected Response Format for AI Agents

When reporting work, use this format:

```text
Summary:
- ...

Files changed:
- ...

Tests:
- ...

Validation:
- ...

Risks or follow-ups:
- ...
