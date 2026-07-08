# 09 - Frontend Simulation Prompt

## Purpose

Use this prompt to implement the Angular pricing simulation screen for the **SRM Credit Engine**.

This prompt focuses on:

* pricing simulation form;
* backend integration with `POST /api/pricing/simulations`;
* loading, success and error states;
* display of backend-calculated values.

## Target Branch

Recommended branch:

```text
feature/frontend-simulation
```

Recommended commit message:

```text
feat: add pricing simulation screen
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to implement only the Angular pricing simulation screen.

Read first:
- AGENTS.md
- README.md
- AI_USAGE.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/06-architecture.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-005-frontend-stack.md
- docs/adr/ADR-007-ai-assisted-development.md

Task:
Implement the pricing simulation UI.

Scope:
You may change:
- frontend/src/app/features/pricing-simulation
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
- Call POST /api/pricing/simulations.
- Use backend reference-data endpoints for options.
- Display present value, discount, payment value, spread, term, exchange rate and calculation timestamp.
- Display backend validation and business errors clearly.
- Show loading state.

Constraints:
- Do not implement the official pricing formula in Angular.
- Do not hardcode spreads as the business source of truth.
- Do not invent a frontend fallback base rate.

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
