# 10 - Frontend Statement Grid Prompt

## Purpose

Use this prompt to implement the Angular settlement statement grid for the **SRM Credit Engine**.

This prompt focuses on:

* server-side filters;
* backend pagination;
* empty/loading/error states;
* settlement summary display.

## Target Branch

Recommended branch:

```text
feature/frontend-statement-grid
```

Recommended commit message:

```text
feat: add settlement statement grid
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to implement only the Angular settlement statement grid.

Read first:
- AGENTS.md
- README.md
- AI_USAGE.md
- docs/specs/04-api-contract.md
- docs/specs/06-architecture.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-005-frontend-stack.md

Task:
Implement the settlement statement screen using backend pagination.

Scope:
You may change:
- frontend/src/app/features/settlements
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
- Call GET /api/settlements/statement.
- Support documented filters.
- Request data again on page and filter changes.
- Use server-side pagination only.
- Display total results and page information if useful.
- Display loading, empty and error states.

Constraints:
- Do not fetch all rows and paginate locally.
- Do not invent local filtering over the full dataset.
- Do not recalculate any financial values in the frontend.

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
