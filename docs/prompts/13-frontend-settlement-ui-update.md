# 14 - Frontend Settlement UI, PT-BR and Visual Cleanup Prompt

## Purpose

Use this prompt to evolve the Angular frontend of the **SRM Credit Engine** beyond the initial simulation/history/currency screens.

This prompt focuses on:

* settlement batch registration UI;
* settlement detail UI for audit snapshot inspection;
* frontend translation to Portuguese, except technical terms;
* visual cleanup across all screens by removing challenge/test-oriented banners and explanatory blocks that do not help the operator workflow.

## Target Branch

Recommended branch:

```text
feature/frontend-settlement-ui-update
```

Recommended commit messages:

```text
feat: add settlement creation and detail screens
refactor: translate frontend to pt-br
style: simplify operator-facing frontend copy
```

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to implement the missing operational settlement flow in the Angular frontend, add the settlement detail view, translate the UI to Portuguese where appropriate, and simplify the operator-facing copy.

Read first:
- AGENTS.md
- README.md
- AI_USAGE.md
- C:\Users\heito\Downloads\README_case_dev_srm.md
- docs/specs/01-product-brief.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/06-architecture.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-005-frontend-stack.md
- docs/adr/ADR-007-ai-assisted-development.md

Task:
Implement the frontend settlement registration flow and settlement detail screen, then localize and simplify the operator UI.

Scope:
You may change:
- frontend/src/app/app.routes.ts
- frontend/src/app/core/layout
- frontend/src/app/core/api only if a shared helper is strictly necessary
- frontend/src/app/features/settlements
- frontend/src/app/features/pricing-simulation
- frontend/src/app/features/exchange-rates
- frontend/src/app/models
- frontend/src/app/shared only if shared UI helpers are strictly necessary
- frontend/src/styles.scss if global visual cleanup is strictly necessary
- AI_USAGE.md only if you materially use AI and need to log the interaction

Do not change:
- backend/
- docs/specs/
- docs/adr/
- docs/diagrams/
- docker-compose.yml
- .env.example unless a frontend runtime variable is truly missing

Functional requirements:
- Implement a settlement batch creation screen that calls POST /api/settlements.
- The settlement form must support:
  - assignor name
  - assignor document
  - payment currency
  - optional base rate
  - one or more receivables
  - add/remove receivable rows
- Each receivable row must support:
  - external reference
  - face value
  - source currency
  - receivable type
  - due date
- Respect the documented batch constraints:
  - minimum 1 receivable
  - maximum 100 receivables
  - one source currency per batch
- Display backend validation and business errors clearly, including duplicate settlement, missing exchange rate and mixed source currency.
- On successful settlement creation, display the returned persisted totals and item snapshots or navigate to the settlement detail screen using the created settlement id.

- Implement a settlement detail screen that calls GET /api/settlements/{id}.
- The detail screen must expose persisted audit information, including:
  - settlement header information
  - assignor data
  - source currency
  - payment currency
  - status
  - base rate
  - totals
  - settledAt
  - item snapshots
- Each item snapshot must display:
  - external reference
  - receivable type
  - face value
  - source currency
  - payment currency
  - base rate
  - spread
  - term in months
  - present value in source currency
  - discount value
  - payment value
  - exchange rate when applicable
  - calculatedAt
- Allow navigation from the settlement statement grid to the settlement detail screen.

Localization requirements:
- Translate the frontend copy to Portuguese.
- Keep technical terms in English when translation would harm clarity or contradict the documented contract.
- Examples of terms that may remain in English when helpful:
  - API
  - backend
  - frontend
  - loading
  - status
  - page size
  - server-side pagination
- Labels, helper texts, button texts, empty states and error headings should become Portuguese-first.

Visual cleanup requirements:
- Remove banners, subtitles, cards or empty-state blocks whose main purpose is to explain that the UI is a technical challenge, frontend shell, base flow, foundation, or test scaffold.
- Keep the forms, tables, buttons, feedback panels and essential operator guidance.
- Prefer concise operational copy over explanatory tutorial-like copy.
- Keep the UI clean and intelligible for an operator or backoffice analyst.

Architecture requirements:
- Keep the existing Angular feature-oriented organization.
- Use Reactive Forms.
- Keep API communication inside services.
- Use server-side pagination for the settlement statement only.
- Keep business-critical calculations in the backend.
- Do not implement official pricing or settlement formulas in Angular.
- Reuse shared UI helpers only when they improve clarity and avoid duplication.

Routing requirements:
- Add routes for settlement creation and settlement detail in a way that fits the current layout.
- Keep existing simulation, settlements statement and exchange-rate routes working.
- Update navigation labels to Portuguese and keep navigation understandable.

API requirements:
- Use POST /api/settlements for settlement creation.
- Use GET /api/settlements/{id} for settlement detail.
- Keep GET /api/settlements/statement integration intact.
- Keep POST /api/pricing/simulations integration intact.
- Keep POST /api/exchange-rates and optional GET /api/exchange-rates/latest intact.

UX requirements:
- Show loading states during requests.
- Show success states when a settlement or exchange rate is created.
- Show empty states only when they add operational value.
- Keep currency context visible for monetary values.
- Keep cross-currency values understandable.
- Preserve clear field-level validation where practical.

Constraints:
- Do not change backend contracts unless a real mismatch is discovered and explicitly reported.
- Do not remove required filters or pagination from the statement grid.
- Do not hide audit-relevant values from the settlement detail response.
- Do not introduce NgRx.
- Do not add authentication or unrelated modules.
- Do not reintroduce verbose “technical challenge” explanatory text in another form.

Tests:
- Add or update component/service tests where practical for:
  - settlement creation form behavior
  - settlement detail API integration
  - statement-to-detail navigation
  - translated labels or major UI states when reasonable
- Run npm run build.
- Run frontend tests.

Expected output:
- Summary
- Files changed
- Tests/build executed
- Validation performed
- Risks or follow-ups
```
