# 02 - Database Migrations Prompt

## Purpose

Use this prompt to implement the initial database layer for the **SRM Credit Engine**.

This prompt focuses on:

* Flyway configuration;
* initial schema creation;
* reference-data seed migrations;
* constraints for business invariants;
* indexes for analytical queries;
* schema validation tests.

This task must not implement pricing, settlement, statement or frontend behavior.

## Target Branch

Recommended branch:

```text
feature/database-migrations
```

Recommended commit messages:

```text
feat: add initial database migrations
test: validate database schema migrations
```

## Prerequisites

Before running this prompt, the following should already exist:

```text
[ ] Backend scaffold exists.
[ ] Maven project is configured.
[ ] Flyway dependency is available.
[ ] JPA ddl-auto is intended to validate, not create schema.
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to implement only the initial database migrations and related persistence validation setup.

Read first:
- AGENTS.md
- README.md
- AI_USAGE.md
- docs/specs/03-business-rules.md
- docs/specs/05-data-model.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/specs/08-acceptance-criteria.md
- docs/diagrams/er-diagram.md
- docs/adr/ADR-002-database-choice.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-007-ai-assisted-development.md

Task:
Implement the initial Flyway migrations and schema validation support.

Scope:
You may change:
- backend/src/main/resources/db/migration
- backend/src/main/java only for JPA mappings or persistence config that are strictly required
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

Requirements:
- Create V1__create_initial_schema.sql.
- Create V2__seed_reference_data.sql.
- Use MySQL-compatible DDL.
- Create all required tables from docs/specs/05-data-model.md.
- Use DECIMAL for monetary values, rates and terms.
- Create foreign keys, unique constraints and indexes defined by the data model.
- Seed BRL and USD currencies.
- Seed MERCANTILE_DUPLICATE and POST_DATED_CHECK receivable types with documented spreads.
- Keep settlement_items.receivable_id unique.
- Keep receivables(assignor_id, external_reference) unique.
- Ensure schema supports statement-query filters efficiently.
- Keep Hibernate configured for ddl-auto=validate.

Tests:
- Add migration validation or integration tests as practical.
- Run mvn test or mvn verify.

Constraints:
- Do not use FLOAT, DOUBLE or REAL for financial columns.
- Do not rely on Hibernate auto-DDL as the final schema source.
- Do not implement business logic in this task.
- Do not change the documented schema without reporting a mismatch first.

Expected output:
- Summary
- Files changed
- Tests executed
- Validation performed
- Risks or follow-ups
```
