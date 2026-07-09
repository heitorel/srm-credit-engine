# 00 — Specification Index

## 1. Purpose

This directory contains the specifications for the **SRM Credit Engine** project.

The project follows **Specification-Driven Development**, meaning that implementation should be guided by written specifications, explicit business rules, architecture decisions and acceptance criteria.

Before implementing a feature, the relevant specification files must be read and followed.

## 2. Project Summary

The SRM Credit Engine is a multi-currency credit assignment platform responsible for:

* managing exchange rates;
* simulating receivable pricing;
* applying risk spreads by receivable type;
* settling batches of receivables atomically;
* storing auditable settlement records;
* exposing REST APIs documented with OpenAPI/Swagger;
* providing an Angular frontend for operators;
* supporting analytical settlement statement queries.

The system prioritizes:

* financial precision;
* transaction consistency;
* auditability;
* clear API contracts;
* layered architecture;
* robust validation;
* testability;
* controlled AI-assisted development.

## 3. Specification Reading Order

The recommended reading order is:

```text
00-spec-index.md
01-product-brief.md
02-domain-glossary.md
03-business-rules.md
04-api-contract.md
05-data-model.md
06-architecture.md
07-testing-strategy.md
08-acceptance-criteria.md
09-git-workflow.md
10-ai-workflow.md
11-delivery-checklist.md
```

## 4. Specification Files

### 4.1 `00-spec-index.md`

Defines the purpose of the specification directory, the reading order and the relationship between specification files.

### 4.2 `01-product-brief.md`

Describes the product context, target users, business goals, core capabilities, non-goals and delivery scope.

Use this file to understand what the product is and what problem it solves.

### 4.3 `02-domain-glossary.md`

Defines the main business and technical terms used throughout the project.

Use this file to avoid inconsistent naming in code, API contracts, database tables and documentation.

### 4.4 `03-business-rules.md`

Defines mandatory business rules, especially around:

* monetary precision;
* supported currencies;
* supported receivable types;
* spreads;
* pricing formula;
* term calculation;
* cross-currency conversion;
* exchange-rate handling;
* settlement atomicity;
* auditability;
* duplicate settlement prevention.

This is one of the most important files in the project.

Business logic must not contradict this file.

### 4.5 `04-api-contract.md`

Defines the planned REST API contract, including:

* endpoints;
* request payloads;
* response payloads;
* error responses;
* pagination;
* filtering;
* expected HTTP status codes.

The backend implementation and frontend integration must follow this contract.

### 4.6 `05-data-model.md`

Defines the relational data model, including:

* tables;
* fields;
* relationships;
* indexes;
* constraints;
* migration expectations;
* audit fields.

Flyway migrations must be consistent with this specification.

### 4.7 `06-architecture.md`

Defines the planned application architecture for backend and frontend.

It describes:

* backend layers;
* frontend organization;
* module responsibilities;
* dependency rules;
* transaction boundaries;
* reporting architecture;
* integration boundaries.

### 4.8 `07-testing-strategy.md`

Defines the testing strategy for the project.

It covers:

* unit tests;
* integration tests;
* API tests;
* frontend tests;
* financial calculation test cases;
* transaction consistency test cases;
* expected test commands.

### 4.9 `08-acceptance-criteria.md`

Defines the acceptance criteria used to evaluate whether the project delivery is complete.

It covers:

* functional acceptance;
* usability;
* security;
* performance;
* scalability;
* observability;
* documentation;
* Git workflow;
* AI usage transparency.

### 4.10 `09-git-workflow.md`

Defines the Git strategy used in the project.

It covers:

* branch model;
* Pull Request simulation;
* Conventional Commits;
* release flow;
* tagging;
* history expectations.

### 4.11 `10-ai-workflow.md`

Defines how AI tools may be used during development.

It covers:

* permitted AI usage;
* forbidden AI usage;
* Codex prompting rules;
* AI review checklist;
* required updates to `AI_USAGE.md`.

### 4.12 `11-delivery-checklist.md`

Defines the final checklist before delivery.

It covers:

* backend readiness;
* frontend readiness;
* database readiness;
* Docker readiness;
* documentation readiness;
* testing readiness;
* Git readiness;
* release readiness.

## 5. Relationship with ADRs

Specifications define **what the system must do** and **how requirements are interpreted**.

Architecture Decision Records under `docs/adr/` define **why major technical decisions were made**.

When a specification depends on a major technical decision, the relevant ADR must be consulted.

Expected ADRs:

```text
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-005-frontend-stack.md
docs/adr/ADR-006-git-workflow.md
docs/adr/ADR-007-ai-assisted-development.md
```

## 6. Relationship with Diagrams

Diagrams under `docs/diagrams/` are derived from the specifications and ADRs.

Expected diagrams:

```text
docs/diagrams/er-diagram.md
docs/diagrams/c4-context.md
docs/diagrams/c4-container.md
```

The diagrams must remain consistent with:

* `05-data-model.md`;
* `06-architecture.md`;
* relevant ADRs.

## 7. Relationship with Prompts

Prompts under `docs/prompts/` are intended to guide AI-assisted implementation.

Prompts must reference relevant specifications and ADRs explicitly.

A Codex task should not be executed from a vague prompt. It should reference the exact documents that define the expected behavior.

Example:

```text
Read:
- AGENTS.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/07-testing-strategy.md
- docs/adr/ADR-003-money-precision.md

Task:
Implement only the pricing simulation endpoint.

Constraints:
- Do not use floating-point types for financial calculations.
- Do not place business logic in controllers.
- Add tests for pricing strategies.
```

## 8. Implementation Rule

No implementation task should begin without identifying:

* the relevant specification file;
* the relevant ADR, when applicable;
* the expected acceptance criteria;
* the tests that should be created or updated.

## 9. Change Control

When implementation reveals that a specification is incomplete or incorrect, the project author must update the specification before or alongside the implementation change.

A code change that modifies behavior should not leave the specifications outdated.

Examples:

* If the API response changes, update `04-api-contract.md`.
* If a table or column changes, update `05-data-model.md` and the ER diagram.
* If a business rule changes, update `03-business-rules.md`.
* If the architecture changes, update `06-architecture.md` and the relevant ADR.
* If AI was used meaningfully, update `AI_USAGE.md`.

## 10. Priority of Sources

When files conflict, use this order of authority:

```text
1. Original technical challenge statement
2. docs/specs/03-business-rules.md
3. docs/specs/04-api-contract.md
4. docs/specs/05-data-model.md
5. docs/adr/*
6. AGENTS.md
7. README.md
8. docs/prompts/*
```

If a conflict is detected, stop implementation and update the relevant documentation before continuing.

## 11. Current Phase

```text
Phase: Specification phase
Branch: develop
Current objective: consolidate specifications, ADRs, diagrams and AI prompts before implementation
```

## 12. Definition of Ready for Implementation

The project is ready to start implementation when the following files are complete and consistent:

```text
README.md
AGENTS.md
AI_USAGE.md
.env.example
docker-compose.yml
docs/specs/*
docs/adr/*
docs/diagrams/*
docs/prompts/*
```

At that point, implementation can proceed through feature branches created from `develop`.

## 13. Definition of Done for Specification Phase

The specification phase is complete when:

* all specification files are filled with final initial content;
* all ADRs are filled with accepted decisions;
* diagrams match architecture and data model decisions;
* prompts are scoped and ready for Codex usage;
* `AI_USAGE.md` includes planning-stage AI interactions;
* `README.md` describes project strategy and delivery expectations;
* the full documentation set is committed once to `develop`.
