# 06 - Settlement Detail Prompt

## Purpose

Use this prompt to implement the settlement detail read endpoint for the **SRM Credit Engine**.

This prompt focuses on:

* `GET /api/settlements/{id}`;
* persisted audit snapshot response;
* structured `404` behavior;
* tests proving detail uses stored values instead of recalculation.

This task must not implement settlement creation, statement query or frontend behavior.

## Target Branch

Recommended branch:

```text
feature/settlement-detail
```

Recommended commit messages:

```text
feat: add settlement detail query
test: cover settlement detail audit response
```

## Prerequisites

Before running this prompt, the following should already exist:

```text
[ ] Backend scaffold exists.
[ ] Database migrations exist.
[ ] Settlement flow exists.
[ ] Settlement and settlement_items data are persisted with snapshots.
[ ] Global exception handling exists.
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to implement only settlement detail retrieval.

Read first:
- AGENTS.md
- README.md
- AI_USAGE.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/05-data-model.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-002-database-choice.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-004-architecture-style.md
- docs/adr/ADR-007-ai-assisted-development.md

Task:
Implement GET /api/settlements/{id}.

Scope:
You may change:
- backend/src/main/java
- backend/src/test/java
- backend/pom.xml only if a strictly necessary dependency is missing
- AI_USAGE.md only if you materially use AI and need to log the interaction

Do not change:
- frontend/
- docs/specs/
- docs/adr/
- docs/diagrams/
- docs/prompts/
- docker-compose.yml
- .env.example
- backend/src/main/resources/db/migration unless a missing read-support field blocks the endpoint

Requirements:
- Implement GET /api/settlements/{id} according to docs/specs/04-api-contract.md.
- Return settlement header data and item-level calculation snapshots.
- Read persisted values only.
- Do not recalculate values from current spreads, exchange rates or base rates.
- Return 404 for unknown settlement ids.
- Preserve structured error responses.
- Keep controller thin.

Tests:
- Add API or application tests for existing settlement detail retrieval.
- Add a test proving historical detail does not change after reference data changes.
- Run mvn test.

Constraints:
- Do not implement new settlement write behavior in this task.
- Do not call PricingEngine to reconstruct historical values.
- Do not introduce statement-grid behavior.

Expected output:
- Summary
- Files changed
- Tests executed
- Validation performed
- Risks or follow-ups
```
