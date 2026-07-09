# 15 - Startup Test Data Seed Prompt

## Purpose

Use this prompt to add controlled startup test data for the **SRM Credit Engine**.

This prompt focuses on:

* deterministic seed data for local validation;
* startup loading for non-production environments;
* realistic sample data for exchange rates, assignors and receivables;
* visible initial data for the main frontend screens right after application startup;
* idempotent database initialization behavior;
* tests proving the seed strategy is safe and repeatable.

This task must not change official financial rules or bypass settlement auditability requirements.

## Target Branch

Recommended branch:

```text
feature/startup-test-data-seed
```

Recommended commit messages:

```text
feat: add controlled startup test data
test: validate startup seed loading
docs: document local startup dataset
```

## Prerequisites

Before running this prompt, the following should already exist:

```text
[ ] Backend scaffold exists.
[ ] Flyway migrations for the base schema already exist.
[ ] Reference data seed migration already exists.
[ ] Core settlement and exchange-rate persistence model is available.
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to implement controlled startup test data so the application becomes easier to validate locally without manual database preparation and the frontend does not appear empty immediately after startup.

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
- docs/specs/10-ai-workflow.md
- docs/adr/ADR-002-database-choice.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-004-architecture-style.md
- docs/adr/ADR-007-ai-assisted-development.md

Task:
Implement a startup seed mechanism for local and test environments with realistic database masses that support manual API and frontend validation, including visible initial records for the main frontend screens right after the first local boot.

Scope:
You may change:
- backend/src/main/java only for seed orchestration or profile-based startup wiring that is strictly necessary
- backend/src/main/resources
- backend/src/test/java
- backend/src/test/resources
- README.md only if local startup instructions must be updated
- AI_USAGE.md only if you materially use AI and need to log the interaction

Do not change:
- frontend/
- docs/specs/
- docs/adr/
- docs/diagrams/
- docs/prompts/
- docker-compose.yml unless startup seed activation truly requires an environment variable
- .env.example unless a new documented flag is strictly necessary

Requirements:
- Add a controlled startup mechanism for non-production data loading.
- Keep the seed disabled by default for production-oriented execution.
- Use deterministic and idempotent inserts or equivalent safe loading behavior.
- Seed realistic sample assignors, receivables and exchange rates that allow pricing simulation, settlement creation and statement validation.
- Ensure the local startup dataset is sufficient so the main frontend screens are not empty immediately after initialization.
- Ensure exchange-rate-related screens have visible registered rates on first load.
- Ensure settlement statement or history screens have visible records on first load, which may require seeding complete settlements with auditable snapshots.
- Ensure seeded receivables also support creating additional settlements manually from the frontend after startup.
- Keep sample currencies and receivable types aligned with existing reference data.
- If settlement records are seeded, persist complete auditable snapshots consistent with the documented business rules.
- Ensure receivables are not duplicated across restarts.
- Ensure seeded exchange rates are positive and directionally explicit.
- Use BigDecimal-safe representations for any Java seed constants.
- Keep business logic in the correct layer and do not move financial calculation rules into startup loaders.
- Prefer profile-based or explicit-flag activation over unconditional startup seeding.
- Document how to activate the startup dataset locally if the workflow changes.

Tests:
- Add or update tests for startup seed activation behavior.
- Add or update tests proving idempotent repeated startup behavior.
- Add or update tests proving the seed does not load in the default production-safe mode.
- Run mvn test or mvn verify, depending on project setup.

Constraints:
- Do not use double, float, Double or Float for financial seed values.
- Do not hardcode exchange rates inside pricing logic.
- Do not make test masses mandatory for all environments.
- Do not bypass transactional consistency when seeding settlement-related records.
- Do not change documented business rules unless a real mismatch is found and explicitly reported.
- Keep the diff focused on startup dataset support.

Expected output:
- Summary
- Files changed
- Tests executed
- Validation performed
- Risks or follow-ups
```
