# 11 - Frontend Exchange Rates Prompt

## Purpose

Use this prompt to implement the Angular exchange-rate registration screen for the **SRM Credit Engine**.

This prompt focuses on:

* exchange-rate form;
* backend integration with `POST /api/exchange-rates`;
* optional latest-rate lookup;
* success and error feedback.

## Target Branch

Recommended branch:

```text
feature/frontend-exchange-rates
```

Recommended commit message:

```text
feat: add exchange rate screen
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to implement only the Angular exchange-rate screen.

Read first:
- AGENTS.md
- README.md
- AI_USAGE.md
- docs/specs/04-api-contract.md
- docs/specs/06-architecture.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-005-frontend-stack.md

Task:
Implement the exchange-rate registration UI.

Scope:
You may change:
- frontend/src/app/features/exchange-rates
- frontend/src/app/core/api
- frontend/src/app/models
- frontend/src/app/shared only if shared UI helpers are strictly necessary
- AI_USAGE.md only if you materially use AI and need to log the interaction

Do not change:
- backend/
- docs/
- docker-compose.yml
- .env.example unless a frontend environment variable is missing

Requirements:
- Use Reactive Forms.
- Call POST /api/exchange-rates.
- Optionally support GET /api/exchange-rates/latest.
- Validate required fields and positive rate input.
- Use backend reference-data endpoints for supported currencies.
- Display success and backend errors clearly.

Constraints:
- Do not hardcode exchange rates.
- Do not treat frontend validation as authoritative.

Tests:
- Add component/service tests if practical.
- Run npm run build.

Expected output:
- Summary
- Files changed
- Tests/build executed
- Validation performed
- Risks or follow-ups
```
