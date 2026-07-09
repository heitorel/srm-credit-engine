# 00 — Bootstrap Prompt

## Purpose

Use this prompt to initialize a new AI-assisted implementation session for the **SRM Credit Engine** project.

This prompt should be used before asking Codex or another AI tool to implement features.

The goal is to force the AI agent to understand:

* project context;
* business domain;
* architecture;
* financial constraints;
* Git workflow;
* testing expectations;
* AI usage rules;
* documentation responsibilities.

## Prompt

```text
You are working on the SRM Credit Engine repository.

This project is a technical challenge for a mid-level Software Engineer position.

The system is a multi-currency credit assignment platform that supports:

- exchange-rate management;
- receivable pricing simulation;
- receivable-type risk spreads;
- batch settlement;
- atomic settlement persistence;
- auditable calculation snapshots;
- settlement statement queries;
- Angular frontend integration;
- OpenAPI/Swagger documentation;
- Docker Compose local execution.

Before making any changes, read these files:

- AGENTS.md
- README.md
- AI_USAGE.md
- docs/specs/00-spec-index.md
- docs/specs/01-product-brief.md
- docs/specs/02-domain-glossary.md
- docs/specs/03-business-rules.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/specs/09-git-workflow.md
- docs/specs/10-ai-workflow.md
- docs/adr/ADR-001-backend-stack.md
- docs/adr/ADR-002-database-choice.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-004-architecture-style.md
- docs/adr/ADR-005-frontend-stack.md
- docs/adr/ADR-006-git-workflow.md
- docs/adr/ADR-007-ai-assisted-development.md

After reading, summarize your understanding of the project in the following format:

Summary:
- What the product does.
- What the main business rules are.
- What the backend stack is.
- What the frontend stack is.
- What the database strategy is.
- What the architecture style is.
- What the Git workflow is.
- What the AI workflow rules are.

Critical constraints:
- List the constraints that must not be violated.

Implementation risks:
- List the main risks you must watch for while generating code.

Do not implement anything yet.

Stop after the summary.
```

## Expected AI Response

The AI agent should respond with a concise but complete understanding of the project.

The response should mention at least:

```text
- backend owns official financial calculations;
- financial calculations must use BigDecimal;
- floating-point types must not be used for money, rates or financial results;
- cross-currency conversion happens after present value calculation;
- settlement batches must be atomic;
- settlement item snapshots must be persisted;
- duplicate settlement must be prevented;
- statement queries must use database-level filtering and server-side pagination;
- frontend must not implement the official pricing formula;
- Flyway manages schema;
- MySQL 8.4 LTS is the selected database;
- Java 21 and Spring Boot 4.1.x are used for backend;
- Angular 22 is used for frontend;
- feature branches are created from develop;
- AI usage must be documented in AI_USAGE.md.
```

## When to Use

Use this prompt:

```text
- at the start of a new Codex session;
- before a major implementation phase;
- when switching AI agents;
- when the AI appears to be ignoring project rules;
- before asking for code generation on critical financial logic;
- before final review.
```

## When Not to Use

Do not use this prompt when the task is already highly scoped and the AI has already read the relevant context in the same session.

For scoped implementation tasks, use the specific prompts:

```text
docs/prompts/01-backend-scaffold.md
docs/prompts/02-database-migrations.md
docs/prompts/03-currency-engine.md
docs/prompts/04-pricing-engine.md
docs/prompts/05-settlement-flow.md
docs/prompts/06-settlement-detail.md
docs/prompts/07-statement-query.md
docs/prompts/08-frontend-scaffold.md
docs/prompts/09-frontend-simulation.md
docs/prompts/10-frontend-statement-grid.md
docs/prompts/11-frontend-exchange-rates.md
docs/prompts/12-docker-and-delivery.md
docs/prompts/13-frontend-settlement-ui-update.md
docs/prompts/14-startup-test-data-seed.md
docs/prompts/15-review.md
```

## Notes for the Author

After the AI returns the bootstrap summary:

```text
1. Check if it understood the critical business rules.
2. Check if it mentioned BigDecimal and financial precision.
3. Check if it mentioned transaction atomicity.
4. Check if it mentioned audit snapshots.
5. Check if it mentioned frontend/backend calculation boundary.
6. Check if it mentioned server-side pagination.
7. Correct the AI before proceeding if any critical point is missing.
```

If the AI misses a critical constraint, ask it to reread the relevant spec before implementation.
