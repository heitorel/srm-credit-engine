# 10 - AI Workflow

## 1. Purpose

This document defines how Artificial Intelligence tools may be used during the development of the **SRM Credit Engine**.

The project uses AI as an engineering co-pilot, not as a substitute for technical ownership.

The workflow exists to ensure that AI-assisted development remains:

* scoped;
* transparent;
* auditable;
* test-driven where needed;
* aligned with specs and ADRs;
* safe for financial-domain implementation;
* documented in `AI_USAGE.md`.

## 2. AI Policy Summary

AI usage is allowed and encouraged when it improves engineering productivity.

However:

```text id="ck977s"
The author owns 100% of the delivered code, documentation and decisions.
```

AI output must be treated as a draft.

No AI-generated code or documentation should be committed without review.

## 3. AI Tools

## 3.1 ChatGPT

Expected usage:

* challenge interpretation;
* hidden-risk analysis;
* specification drafting;
* ADR drafting;
* architecture discussion;
* prompt design;
* review checklists;
* documentation review.

## 3.2 Codex

Expected usage:

* scoped implementation tasks;
* code generation for small features;
* unit test generation;
* refactoring support;
* debugging support;
* repository-aware code review;
* consistency checks against specs.

## 3.3 Other AI Tools

Other AI tools may be used if documented.

Examples:

```text id="p7hou1"
IDE autocomplete
LLM-based code review
AI-assisted test generation
```

Material usage must be logged in `AI_USAGE.md`.

## 4. Core AI Development Principles

The following principles are mandatory:

```text id="1evxou"
1. Specs before code.
2. Small prompts before large prompts.
3. Explicit scope before implementation.
4. Tests before accepting business-critical output.
5. Manual review before commit.
6. AI_USAGE.md updated for material AI contribution.
7. No hidden assumptions.
8. No AI-generated financial logic without strict review.
```

## 5. Specification-Driven AI Workflow

Every AI-assisted task should follow this sequence:

```text id="qy2n84"
1. Identify the feature or problem.
2. Identify relevant specs.
3. Identify relevant ADRs.
4. Define the expected behavior.
5. Define allowed files/directories.
6. Define forbidden changes.
7. Define test expectations.
8. Ask AI for scoped work.
9. Review the diff.
10. Run tests/checks.
11. Correct unsafe or wrong output.
12. Update AI_USAGE.md if the contribution was material.
13. Commit using Conventional Commits.
```

## 6. Required Context Files

AI tools must be pointed to relevant context files.

## 6.1 General Context

For most tasks, include:

```text id="oi07yq"
AGENTS.md
README.md
docs/specs/00-spec-index.md
```

## 6.2 Backend Feature Context

For backend implementation tasks, include:

```text id="wk5dnp"
AGENTS.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
```

Add more specific ADRs as needed.

## 6.3 Database Context

For database or migration tasks, include:

```text id="u0m9v5"
AGENTS.md
docs/specs/05-data-model.md
docs/diagrams/er-diagram.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
```

## 6.4 Frontend Context

For frontend tasks, include:

```text id="hmdql4"
AGENTS.md
docs/specs/04-api-contract.md
docs/specs/06-architecture.md
docs/specs/08-acceptance-criteria.md
docs/adr/ADR-005-frontend-stack.md
```

## 6.5 Git and Delivery Context

For Git, release or documentation tasks, include:

```text id="nb09zc"
docs/specs/09-git-workflow.md
docs/specs/11-delivery-checklist.md
docs/adr/ADR-006-git-workflow.md
```

## 6.6 AI Usage Context

For AI workflow or prompt changes, include:

```text id="j0ii70"
AI_USAGE.md
docs/specs/10-ai-workflow.md
docs/adr/ADR-007-ai-assisted-development.md
docs/prompts/*
```

## 7. Prompt Structure

AI prompts for implementation should follow this structure:

```text id="kihvbd"
Context:
You are working on SRM Credit Engine.

Read first:
- <file 1>
- <file 2>
- <file 3>

Task:
Implement only <specific feature or change>.

Scope:
You may change:
- <allowed path 1>
- <allowed path 2>

Do not change:
- <forbidden path 1>
- <forbidden path 2>

Requirements:
- <requirement 1>
- <requirement 2>
- <requirement 3>

Tests:
- Add or update tests for <cases>.
- Run <commands>.

Constraints:
- Do not use floating-point types for financial calculations.
- Do not put business logic in controllers.
- Keep the diff focused.
- Explain assumptions before implementing.

Expected output:
- Summary of changes.
- Files changed.
- Tests executed.
- Risks or follow-ups.
```

## 8. Good Prompt Examples

## 8.1 Backend Scaffold Prompt

```text id="dzgbk5"
Read:
- AGENTS.md
- docs/specs/06-architecture.md
- docs/adr/ADR-001-backend-stack.md

Task:
Create the initial Spring Boot backend scaffold.

Scope:
You may change:
- backend/

Do not change:
- frontend/
- docs/specs/
- docs/adr/

Requirements:
- Java 21
- Spring Boot 4.1.x
- Maven
- package structure matching architecture spec
- OpenAPI dependency
- validation dependency
- Flyway dependency
- MySQL driver
- global exception handler skeleton

Tests:
- Add a context load test.
- Run mvn test.

Expected output:
- Summary
- Files changed
- Tests run
- Risks or follow-ups
```

## 8.2 Pricing Engine Prompt

```text id="8misr1"
Read:
- AGENTS.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/adr/ADR-003-money-precision.md

Task:
Implement only the pricing engine and pricing simulation endpoint.

Scope:
You may change:
- backend/src/main/java
- backend/src/test/java

Do not change:
- frontend/
- docs/

Requirements:
- Use BigDecimal for all financial calculations.
- Implement PricingStrategy.
- Implement MercantileDuplicatePricingStrategy.
- Implement PostDatedCheckPricingStrategy.
- Implement PricingStrategyResolver.
- Implement PricingEngine.
- Implement POST /api/pricing/simulations.
- Use backend as official calculation source.
- Apply cross-currency conversion after present value calculation.

Tests:
- Add unit tests for both strategies.
- Add unit tests for same-currency pricing.
- Add unit tests for cross-currency conversion order.
- Add tests for missing exchange rate.
- Run mvn test.

Constraints:
- Do not use double or float.
- Do not put pricing logic in controllers.
- Do not hardcode exchange rates.
```

## 8.3 Settlement Flow Prompt

```text id="iqvxln"
Read:
- AGENTS.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/05-data-model.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md

Task:
Implement only the settlement creation flow.

Scope:
You may change:
- backend/src/main/java
- backend/src/test/java
- backend/src/main/resources/db/migration only if schema changes are necessary

Requirements:
- Implement POST /api/settlements.
- Use CreateSettlementService as transaction boundary.
- Validate batch size.
- Enforce one source currency per batch.
- Prevent duplicate settlement.
- Persist settlement item calculation snapshots.
- Update receivable status to SETTLED.
- Rollback full operation if any item fails.

Tests:
- Add integration test for valid settlement.
- Add rollback test for invalid item.
- Add duplicate settlement test.
- Run mvn verify or mvn test, depending on project setup.

Constraints:
- Do not persist partial settlement.
- Do not recalculate historical values dynamically.
- Do not use floating-point types.
```

## 8.4 Frontend Simulation Prompt

```text id="2q6cjy"
Read:
- AGENTS.md
- docs/specs/04-api-contract.md
- docs/specs/06-architecture.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-005-frontend-stack.md

Task:
Implement only the Angular pricing simulation screen.

Scope:
You may change:
- frontend/src/app/features/pricing-simulation
- frontend/src/app/core/api
- frontend/src/app/models

Do not change:
- backend/
- docs/

Requirements:
- Use Angular Reactive Forms.
- Call POST /api/pricing/simulations.
- Display returned pricing values.
- Display backend errors.
- Show loading state.
- Use Angular Material components.

Tests:
- Add tests for form validation or API service if feasible.
- Run npm run build.

Constraints:
- Do not implement the official pricing formula in frontend.
- Do not hardcode exchange rates.
- Do not hardcode spreads.
```

## 9. Bad Prompt Examples

Avoid prompts like:

```text id="0eqvrb"
Build the whole project.
```

```text id="q4u4u0"
Create backend and frontend completely.
```

```text id="guu5j5"
Fix everything.
```

```text id="gf3s67"
Make this production-ready.
```

```text id="20wybo"
Implement all requirements however you think is best.
```

Why these are bad:

* scope is too broad;
* assumptions become hidden;
* generated diffs become hard to review;
* tests are often incomplete;
* architecture may drift;
* financial rules may be implemented unsafely.

## 10. AI Review Checklist

Before accepting AI-generated output, verify:

```text id="uk09wh"
- Did the output follow the requested scope-
- Were unrelated files modified-
- Were relevant specs followed-
- Were relevant ADRs followed-
- Did the code preserve layered architecture-
- Did financial code use BigDecimal-
- Did the code avoid double/float-
- Were percentages represented as decimal rates-
- Was cross-currency conversion applied after present value calculation-
- Were exchange rates retrieved instead of hardcoded-
- Was exchange-rate snapshotting preserved-
- Was settlement atomicity preserved-
- Was duplicate settlement prevention preserved-
- Were backend validations added-
- Were structured errors preserved-
- Were tests added or updated-
- Were tests meaningful-
- Were docs updated if behavior changed-
- Was AI_USAGE.md updated if needed-
```

## 11. High-Risk AI Output Patterns

## 11.1 Floating-Point Financial Calculations

Reject code like:

```java id="q3yq69"
double presentValue = faceValue / Math.pow(1 + baseRate + spread, term);
```

Reject code like:

```java id="4bp0wg"
float rate = 0.015f;
```

Expected:

```text id="zyq7s5"
BigDecimal-based financial calculation with explicit scale and rounding.
```

## 11.2 Unsafe BigDecimal Construction

Reject code like:

```java id="33fr3u"
new BigDecimal(0.015)
```

Expected for constants:

```java id="6f80mx"
new BigDecimal("0.01500000")
```

## 11.3 Wrong Percentage Representation

Reject:

```text id="adywo8"
1.5% represented as 1.5
2.5% represented as 2.5
```

Expected:

```text id="cqro8n"
1.5% represented as 0.01500000
2.5% represented as 0.02500000
```

## 11.4 Wrong Currency Conversion Order

Reject:

```text id="jdzu7v"
Convert face value first, then calculate present value.
```

Expected:

```text id="c6lu5y"
Calculate present value in source currency first, then convert at the end.
```

## 11.5 Missing Exchange-Rate Snapshot

Reject settlement code that only stores:

```text id="hshboh"
exchangeRateId
```

without storing the actual rate used.

Expected:

```text id="xu2pi5"
settlement_items.exchange_rate stores the rate snapshot used at settlement time.
```

## 11.6 Business Logic in Controllers

Reject controllers that:

* calculate present value;
* choose spread;
* convert currencies;
* manage transactions;
* update receivable status directly.

Expected:

```text id="vjkrc7"
Controller -> Application Service -> Domain -> Infrastructure
```

## 11.7 In-Memory Analytical Filtering

Reject:

```java id="u2ji7g"
repository.findAll().stream().filter(...)
```

for statement queries.

Expected:

```text id="e9fvbm"
database-level filtering with pagination.
```

## 11.8 Frontend Financial Formula

Reject Angular code that implements:

```text id="8o1alc"
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

Expected:

```text id="lsgvyb"
frontend calls POST /api/pricing/simulations and displays response.
```

## 11.9 Overengineering

Reject AI suggestions that add unnecessary:

```text id="3lpt04"
Kafka
Kubernetes
microservices
event sourcing
full CQRS
NgRx
distributed tracing
```

unless the specs and ADRs are updated and the addition does not compromise the required delivery.

## 12. AI Usage Logging

## 12.1 When to Update `AI_USAGE.md`

Update `AI_USAGE.md` when AI materially contributes to:

* planning;
* specification;
* architecture;
* implementation;
* tests;
* debugging;
* refactoring;
* review;
* documentation.

Do not ignore AI usage because it produced an accepted result.

Do not ignore AI usage because it produced a rejected result if the failure revealed a meaningful risk.

## 12.2 When Detailed Logging Is Not Required

Detailed logging is not required for:

* minor autocomplete suggestions;
* trivial wording suggestions;
* formatting assistance;
* small typo corrections.

However, if in doubt, add a concise entry.

## 12.3 Required Log Format

Use this structure:

```md id="qf9w17"
### YYYY-MM-DD - <Short description>

**Tool used:** <ChatGPT | Codex | Other>

**Category:** <Planning | Specification | Architecture | Scaffolding | Implementation | Testing | Debugging | Refactoring | Review | Documentation>

**Prompt summary:**

<Describe the prompt.>

**AI contribution:**

<Describe what the AI produced.>

**Author review:**

<Describe how the result was checked.>

**Accepted changes:**

<List accepted changes.>

**Rejected or corrected AI output:**

<List hallucinations, unsafe code, wrong assumptions or rejected suggestions.>

**Tests or validation performed:**

<List commands or checks.>

**Known limitations:**

<List remaining risks or follow-ups.>
```

## 13. AI Usage Categories

Use the following categories consistently:

| Category       | Meaning                                                   |
| -------------- | --------------------------------------------------------- |
| Planning       | Requirements interpretation, risk analysis, task planning |
| Specification  | Writing or reviewing specs                                |
| Architecture   | ADRs, diagrams, design decisions                          |
| Scaffolding    | Initial project or module structure                       |
| Implementation | Feature code generation                                   |
| Testing        | Test generation or review                                 |
| Debugging      | Error investigation                                       |
| Refactoring    | Behavior-preserving improvement                           |
| Review         | Diff/code/design review                                   |
| Documentation  | README, setup, final docs                                 |

## 14. AI Issue Severity

Use severity levels when documenting AI problems.

| Severity | Meaning                                                   |
| -------- | --------------------------------------------------------- |
| Low      | Minor wording, formatting or naming problem               |
| Medium   | Incorrect implementation detail with limited impact       |
| High     | Incorrect business rule, API behavior or test expectation |
| Critical | Financial, transaction, auditability or security defect   |

Examples:

```text id="8x6w0y"
Critical: AI generated double-based financial calculation.
Critical: AI applied currency conversion before present value.
High: AI implemented in-memory statement filtering.
Medium: AI forgot to update OpenAPI annotations.
Low: AI suggested inconsistent class name.
```

## 15. AI and Testing Requirements

When AI generates or changes business-critical code, it must also generate or update tests.

Business-critical areas:

```text id="cv3ov0"
pricing strategies
pricing engine
currency conversion
settlement atomicity
duplicate settlement prevention
exchange-rate lookup
statement query filtering
API validation
```

If AI does not produce tests, either:

* ask for a second scoped prompt to add tests; or
* write tests manually before accepting the change.

## 16. AI and Documentation Requirements

When AI changes behavior, documentation must be checked.

Examples:

| AI change                 | Documentation to check                                   |
| ------------------------- | -------------------------------------------------------- |
| New endpoint              | `04-api-contract.md`, README, Swagger                    |
| New table/column          | `05-data-model.md`, ER diagram                           |
| Changed business rule     | `03-business-rules.md`                                   |
| Changed architecture      | `06-architecture.md`, ADRs, C4 diagrams                  |
| Changed Git workflow      | `09-git-workflow.md`                                     |
| Changed test strategy     | `07-testing-strategy.md`                                 |
| Changed frontend behavior | `04-api-contract.md`, `06-architecture.md`, frontend ADR |

## 17. AI and Git Workflow

AI-assisted tasks must follow the Git workflow.

Rules:

* create feature branch from `develop`;
* use scoped AI prompts;
* review AI output;
* run tests;
* update `AI_USAGE.md`;
* commit with Conventional Commit;
* mention AI usage in PR description when material.

## 18. AI and Pull Requests

PR descriptions must include an AI section when AI materially contributed.

Recommended section:

```md id="wbxhrg"
## AI Usage

AI was used for:
- <purpose>

Review performed:
- <manual checks>
- <tests run>
- <corrections applied>

AI_USAGE.md:
- [ ] Updated
- [ ] Not applicable
```

## 19. AI and Security Review

AI-generated code must be reviewed for:

```text id="44xld6"
- committed secrets;
- unsafe CORS;
- exposed stack traces;
- missing backend validation;
- SQL injection risk;
- hardcoded credentials;
- unsafe Docker defaults;
- overly verbose logs;
- sensitive data exposure.
```

## 20. AI and Dependency Review

AI may suggest dependencies.

Before accepting a dependency, verify:

```text id="p7jevc"
- Is it necessary-
- Is it maintained-
- Is it compatible with the stack-
- Is there a Spring Boot managed alternative-
- Does it add risk or complexity-
- Can the task be solved without it-
```

Avoid adding libraries for trivial tasks.

## 21. AI and Assumption Management

AI must not silently introduce assumptions.

Assumptions must be documented when they affect:

```text id="3pj8e3"
term calculation
rounding mode
base rate source
exchange-rate direction
batch size
settlement status flow
frontend runtime configuration
error response format
```

Relevant files to update:

```text id="f9edum"
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/adr/*
```

## 22. AI-Assisted Development Phases

## 22.1 Specification Phase

Allowed AI support:

* write initial docs;
* review consistency;
* identify missing requirements;
* create prompt templates.

Required checks:

* compare with original challenge;
* remove unsupported claims;
* mark assumptions;
* keep scope realistic.

## 22.2 Backend Scaffold Phase

Allowed AI support:

* generate Maven project;
* configure Spring Boot;
* configure package structure;
* add dependencies;
* add Dockerfile;
* add context test.

Required checks:

* dependency versions;
* package naming;
* Spring Boot startup;
* no premature business logic;
* no secrets.

## 22.3 Database Phase

Allowed AI support:

* draft Flyway migrations;
* create JPA entities;
* create repositories;
* create seed data;
* create integration tests.

Required checks:

* DECIMAL columns;
* foreign keys;
* unique constraints;
* indexes;
* MySQL compatibility;
* schema matches `05-data-model.md`.

## 22.4 Currency Engine Phase

Allowed AI support:

* create exchange-rate DTOs;
* create controller;
* create service;
* create repository methods;
* create tests.

Required checks:

* explicit direction;
* positive rate validation;
* latest lookup ordering;
* no implicit inversion.

## 22.5 Pricing Engine Phase

Allowed AI support:

* create value objects;
* create pricing strategies;
* create pricing engine;
* create simulation endpoint;
* create tests.

Required checks:

* no floating-point financial calculations;
* correct spread values;
* correct conversion order;
* explicit rounding;
* enough test coverage.

## 22.6 Settlement Phase

Allowed AI support:

* create settlement service;
* create endpoint;
* create persistence mapping;
* create rollback tests;
* create duplicate-prevention tests.

Required checks:

* transaction boundary;
* no partial persistence;
* audit snapshots;
* receivable status update;
* database constraint handling.

## 22.7 Statement Query Phase

Allowed AI support:

* create query repository;
* create filters;
* create pagination response;
* create tests.

Required checks:

* no in-memory filtering;
* database-level pagination;
* indexes match query;
* deterministic sorting.

## 22.8 Frontend Phase

Allowed AI support:

* Angular components;
* reactive forms;
* API services;
* Material UI;
* error display;
* frontend tests.

Required checks:

* no official pricing formula in frontend;
* typed API models;
* server-side pagination;
* clear loading/error states.

## 22.9 Final Review Phase

Allowed AI support:

* documentation review;
* checklist review;
* risk review;
* README polishing;
* AI_USAGE review.

Required checks:

* final instructions are accurate;
* tests pass;
* Docker Compose runs;
* Git history is coherent;
* AI usage is transparent.

## 23. Minimum AI Documentation for Final Delivery

Before final delivery, `AI_USAGE.md` must include:

```text id="yq6slh"
- Planning-stage AI usage.
- Specification-stage AI usage.
- Implementation-stage AI usage, if any.
- Testing or review AI usage, if any.
- Rejected or corrected AI output.
- Critical analysis of where AI helped.
- Critical analysis of where AI was risky or harmful.
- Final ownership statement.
```

## 24. Definition of Done for AI-Assisted Task

An AI-assisted task is done when:

```text id="fyedtl"
- AI output was reviewed.
- Unsafe output was rejected or corrected.
- Relevant tests were added or updated.
- Relevant commands were run.
- Documentation was updated if behavior changed.
- AI_USAGE.md was updated if contribution was material.
- Commit follows Conventional Commits.
- PR mentions AI usage when material.
```

## 25. Related Documents

```text id="f1lhbu"
README.md
AGENTS.md
AI_USAGE.md
docs/specs/00-spec-index.md
docs/specs/03-business-rules.md
docs/specs/07-testing-strategy.md
docs/specs/09-git-workflow.md
docs/specs/11-delivery-checklist.md
docs/adr/ADR-007-ai-assisted-development.md
docs/prompts/00-bootstrap.md
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

## 26. Change Policy

If AI workflow changes, update:

```text id="sqx7oi"
docs/specs/10-ai-workflow.md
docs/adr/ADR-007-ai-assisted-development.md
AGENTS.md
AI_USAGE.md
docs/prompts/*
README.md if user-facing policy changes
```

Any change must preserve:

```text id="mwgl55"
transparency
scope control
manual review
test discipline
author ownership
```
