# 13 - Review Prompt

## Purpose

Use this prompt to perform a final AI-assisted review of the **SRM Credit Engine** before delivery.

This prompt is intended for the final review phase, after backend, frontend, database, documentation, tests and Git workflow are mostly complete.

The review must check:

* requirement coverage;
* business-rule consistency;
* financial precision;
* architecture adherence;
* API contract alignment;
* database model alignment;
* settlement atomicity;
* auditability;
* frontend/backend boundaries;
* test coverage;
* documentation completeness;
* Git workflow;
* AI usage transparency;
* delivery readiness.

This prompt must not be used to make broad automatic changes without review.

The expected output is a structured review report, not an immediate implementation patch.

## Target Branch

Recommended branch:

```text id="bpr3o9"
docs/final-delivery
```

or, if reviewing implementation before final merge:

```text id="dgo2sa"
review/final-readiness
```

Recommended commit message if review findings generate documentation updates:

```text id="2b7zg4"
docs: finalize technical challenge delivery
```

Recommended commit message if review findings generate fixes:

```text id="4tv9kw"
fix: address final review findings
```

## Prerequisites

Before running this prompt, the project should have:

```text id="tfcs8h"
[ ] Backend implementation.
[ ] Frontend implementation.
[ ] Database migrations.
[ ] Docker Compose setup.
[ ] Tests.
[ ] README.
[ ] AI_USAGE.md.
[ ] Specs.
[ ] ADRs.
[ ] Diagrams.
[ ] Git workflow mostly complete.
```

If a major feature is still missing, use the specific implementation prompt first:

```text id="j03ayt"
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
```

## Prompt

```text id="w5wlwi"
You are working on the SRM Credit Engine repository.

This task is to perform a final delivery review.

Do not implement changes yet.

First, inspect the repository and read these files:

- AGENTS.md
- README.md
- AI_USAGE.md
- docker-compose.yml
- .env.example
- docs/specs/00-spec-index.md
- docs/specs/01-product-brief.md
- docs/specs/02-domain-glossary.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/05-data-model.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/specs/08-acceptance-criteria.md
- docs/specs/09-git-workflow.md
- docs/specs/10-ai-workflow.md
- docs/specs/11-delivery-checklist.md
- docs/adr/ADR-001-backend-stack.md
- docs/adr/ADR-002-database-choice.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-004-architecture-style.md
- docs/adr/ADR-005-frontend-stack.md
- docs/adr/ADR-006-git-workflow.md
- docs/adr/ADR-007-ai-assisted-development.md
- docs/diagrams/er-diagram.md
- docs/diagrams/c4-context.md
- docs/diagrams/c4-container.md

Then inspect the implementation under:

- backend/
- frontend/

Review the project against the documented specifications.

Do not make code changes automatically.

Produce a structured review report with the following sections:

1. Executive Summary
2. Delivery Readiness
3. Critical Issues
4. High-Priority Issues
5. Medium-Priority Issues
6. Documentation Drift
7. Test Coverage Gaps
8. Financial Precision Review
9. Settlement Atomicity Review
10. API Contract Review
11. Data Model Review
12. Frontend Review
13. Docker and Local Execution Review
14. Security Review
15. AI Usage Review
16. Git Workflow Review
17. Recommended Fix Order
18. Final Go/No-Go Recommendation

Review criteria:

Functional requirements:
- Exchange-rate management exists.
- Latest exchange-rate lookup uses exact direction.
- Pricing simulation exists.
- Pricing uses Strategy Pattern.
- Settlement batch creation exists.
- Settlement creation is atomic.
- Settlement item snapshots are persisted.
- Duplicate settlement is prevented.
- Settlement detail returns persisted values.
- Settlement statement query exists.
- Statement query uses database-level filtering.
- Statement query uses server-side pagination.
- Angular frontend exists.
- Frontend calls backend APIs.

Financial rules:
- Financial calculations use BigDecimal.
- No financial calculation uses double, float, Double or Float.
- BigDecimal is not constructed from floating-point literals.
- MERCANTILE_DUPLICATE spread is 0.01500000.
- POST_DATED_CHECK spread is 0.02500000.
- Present value formula is implemented.
- Discount is face value minus present value.
- Cross-currency conversion happens after present value calculation.
- Same-currency operation does not require exchange rate.
- Missing exchange rate fails cross-currency operation.
- Historical settlements are not recalculated from current exchange rates.

Architecture rules:
- Controllers are thin.
- Business logic is not in controllers.
- Application services orchestrate use cases.
- Domain layer owns business rules.
- Infrastructure layer owns persistence details.
- Settlement transaction boundary is in application service.
- Frontend does not implement official pricing formula.
- Statement query does not use in-memory filtering.

Database rules:
- Flyway migrations exist.
- Hibernate ddl-auto is validate.
- MySQL is configured.
- Monetary and rate columns use DECIMAL.
- Required tables exist.
- Reference data is seeded.
- settlement_items.receivable_id is unique.
- receivables assignor_id + external_reference is unique.
- Statement query indexes exist or are justified.

API rules:
- Endpoints match docs/specs/04-api-contract.md.
- Request DTOs match documented fields.
- Response DTOs match documented fields.
- API returns structured errors.
- Missing exchange rate returns appropriate business error.
- Duplicate settlement returns 409.
- Unknown settlement returns 404.
- Invalid statement date range returns 400.
- OpenAPI/Swagger is available.

Frontend rules:
- Angular version matches ADR.
- Angular Material is configured.
- Reactive Forms are used.
- API services are typed.
- Pricing simulation screen calls backend.
- Statement grid uses server-side pagination.
- Backend errors are displayed clearly.
- Monetary values show currency context.
- No official financial formula exists in frontend code.
- No hardcoded exchange rates exist in frontend code.

Testing rules:
- Pricing strategies are tested.
- Pricing engine is tested.
- Cross-currency conversion order is tested.
- Missing exchange-rate behavior is tested.
- Settlement atomicity is tested.
- Duplicate settlement prevention is tested.
- Statement query filters are tested.
- Statement pagination is tested.
- API validation errors are tested.
- Frontend build passes.
- Backend tests pass.

Docker/local execution:
- docker compose config works.
- docker compose up --build starts mysql, backend and frontend.
- MySQL has healthcheck.
- Backend depends on healthy MySQL.
- Frontend is accessible on port 4200.
- Backend is accessible on port 8080.
- Swagger is accessible.
- Environment variables match .env.example.

Security:
- No real secrets are committed.
- .env is ignored.
- Stack traces are not exposed in API responses.
- CORS is explicit.
- SQL queries do not use unsafe string concatenation.
- Logs do not expose credentials.

AI usage:
- AI_USAGE.md exists.
- AI usage is specific and not superficial.
- Prompts are documented.
- Rejected or corrected AI output is documented.
- Critical analysis exists.
- Final ownership statement exists.

Git workflow:
- Commit messages follow Conventional Commits.
- Feature branches or PRs are represented.
- develop is integration branch.
- main is release branch.
- Final tag v1.0.0 is planned or exists.
- History is readable.
- No single giant implementation commit hides the process.

For each issue found, provide:

- ID
- Severity: Critical, High, Medium, Low
- Area
- File or path
- Description
- Why it matters
- Suggested fix
- Related spec or ADR

Severity rules:
- Critical: blocks delivery or risks wrong financial behavior, data corruption, settlement inconsistency, hidden AI usage or inability to run the project.
- High: weakens evaluation materially but can be fixed without redesign.
- Medium: improvement recommended before delivery.
- Low: polish or optional improvement.

Expected output format:

# Final Review Report

## 1. Executive Summary

## 2. Delivery Readiness

Status:
- Go
- Go with fixes
- No-go

## 3. Critical Issues

| ID | Area | File/Path | Issue | Suggested Fix | Related Spec |
|---|---|---|---|---|---|

## 4. High-Priority Issues

| ID | Area | File/Path | Issue | Suggested Fix | Related Spec |
|---|---|---|---|---|---|

## 5. Medium-Priority Issues

| ID | Area | File/Path | Issue | Suggested Fix | Related Spec |
|---|---|---|---|---|---|

## 6. Low-Priority Issues

## 7. Documentation Drift

## 8. Test Coverage Gaps

## 9. Financial Precision Review

## 10. Settlement Atomicity Review

## 11. API Contract Review

## 12. Data Model Review

## 13. Frontend Review

## 14. Docker and Local Execution Review

## 15. Security Review

## 16. AI Usage Review

## 17. Git Workflow Review

## 18. Recommended Fix Order

## 19. Final Go/No-Go Recommendation

Do not claim something passes unless you verified it by inspecting files or running commands.

If you cannot verify something, mark it as:
- Not verified

If commands are run, include:
- command
- result
- relevant output summary

If commands cannot be run, explain why.
```

## Expected AI Output

The AI should return a review report, not a patch.

The report should be direct and specific.

It should classify issues by severity and reference the relevant files/specs.

The review should not say the project is ready unless it has enough evidence.

## Required Review Areas

## 1. Financial Precision Review

The AI must inspect backend code for unsafe financial patterns.

Search for:

```text id="kokzlv"
double
float
Double
Float
Math.pow
new BigDecimal(
BigDecimal.valueOf(
```

Review context before flagging.

Unsafe examples:

```java id="q3pwwq"
new BigDecimal(0.015)
double presentValue
float exchangeRate
Math.pow(1 + baseRate, term)
```

Potentially acceptable examples:

```java id="05ps3v"
BigDecimal.valueOf(100L)
new BigDecimal("0.01500000")
```

Required finding if unsafe code exists:

```text id="6j0opz"
Critical issue.
```

## 2. Strategy Pattern Review

Verify existence or equivalent implementation of:

```text id="6rxqq7"
PricingStrategy
MercantileDuplicatePricingStrategy
PostDatedCheckPricingStrategy
PricingStrategyResolver
```

Flag as Critical if pricing is implemented only through controller logic or a large unstructured conditional.

## 3. Currency Conversion Order Review

Verify that cross-currency pricing follows:

```text id="3sb3zm"
1. Calculate present value in source currency.
2. Convert present value to payment currency.
```

Flag as Critical if implementation converts face value first.

## 4. Settlement Atomicity Review

Verify:

```text id="nswhzd"
- CreateSettlementService has transaction boundary.
- Settlement is all-or-nothing.
- Invalid item does not persist partial settlement.
- Duplicate settlement is prevented.
- settlement_items.receivable_id unique constraint exists.
```

Flag as Critical if settlement can partially persist.

## 5. Audit Snapshot Review

Verify settlement item stores:

```text id="sy5wwr"
face value
source currency
payment currency
base rate
spread
term
present value
discount value
payment value
exchange rate
calculated at
```

Flag as Critical if historical settlement detail depends on recalculation from current rates.

## 6. Statement Query Review

Verify statement query does not use:

```java id="ybzh9v"
findAll().stream().filter(...)
```

for production filtering.

Verify database-level pagination.

Flag as Critical if statement query loads all rows and filters/paginates in memory.

## 7. Frontend Boundary Review

Search frontend code for signs of official calculation logic.

Suspicious terms:

```text id="inlvu1"
presentValue
Math.pow
baseRate + spread
discount
exchangeRate * present
```

Review context.

Flag as Critical if Angular implements official pricing formula.

## 8. API Contract Review

Compare implemented endpoints against:

```text id="sfblpz"
docs/specs/04-api-contract.md
```

Required endpoints:

```text id="vnpi6q"
GET  /api/reference-data/currencies
GET  /api/reference-data/receivable-types
POST /api/exchange-rates
GET  /api/exchange-rates/latest
POST /api/pricing/simulations
POST /api/settlements
GET  /api/settlements/{id}
GET  /api/settlements/statement
```

Flag missing core endpoints as High or Critical depending on impact.

## 9. Database Review

Verify migrations align with:

```text id="dc8txj"
docs/specs/05-data-model.md
docs/diagrams/er-diagram.md
```

Required checks:

```text id="o7tib2"
- tables exist;
- seed data exists;
- DECIMAL columns exist;
- unique constraints exist;
- indexes exist;
- ddl-auto=validate is configured.
```

## 10. Test Review

Verify test coverage for:

```text id="vazd7m"
- pricing strategies;
- same-currency pricing;
- cross-currency pricing;
- conversion order;
- missing exchange rate;
- invalid due date;
- settlement rollback;
- duplicate settlement;
- statement filtering;
- statement pagination;
- API validation errors.
```

Flag missing business-critical tests as High.

Flag missing pricing strategy tests as Critical if pricing is implemented.

## 11. Docker Review

Verify:

```text id="8pdukg"
docker compose config
docker compose up --build
```

If commands cannot be run, mark runtime as Not verified.

## 12. Documentation Review

Compare implementation against:

```text id="7gws8k"
README.md
docs/specs/*
docs/adr/*
docs/diagrams/*
```

Flag drift when docs claim behavior that code does not implement.

## 13. AI Usage Review

Verify `AI_USAGE.md` includes:

```text id="ls4gt1"
- tools used;
- prompt summaries;
- AI contribution;
- author review;
- accepted changes;
- rejected or corrected output;
- validation performed;
- critical analysis;
- final ownership statement.
```

Flag superficial AI usage documentation as High.

## 14. Git Review

Review commit messages and branch flow.

Required:

```text id="v8ggfg"
- Conventional Commits;
- develop integration branch;
- feature branch evidence;
- final main merge;
- tag v1.0.0.
```

If Git metadata is unavailable to the AI, mark as Not verified and provide manual checks.

## Optional Fix Prompt

After the review report, if the author asks to fix issues, use this follow-up prompt pattern:

```text id="n5xw6c"
Based on the final review report, fix only issue <ID>.

Read:
- <relevant spec>
- <relevant ADR>
- <affected files>

Scope:
You may change:
- <specific files or directories>

Do not change:
- <unrelated areas>

Requirements:
- <specific fix requirements>

Tests:
- Add or update tests for the issue.
- Run <command>.

Expected output:
- Summary
- Files changed
- Tests run
- Remaining risks
```

Do not ask AI to fix all issues at once unless the issues are documentation-only and low-risk.

## Acceptance Criteria

This review task is acceptable when:

```text id="sjbze1"
[ ] AI inspected specs and ADRs.
[ ] AI inspected backend implementation.
[ ] AI inspected frontend implementation.
[ ] AI inspected database migrations.
[ ] AI inspected README and AI_USAGE.md.
[ ] AI inspected Docker configuration.
[ ] AI produced issue list with severity.
[ ] AI identified financial precision risks.
[ ] AI identified settlement atomicity risks.
[ ] AI identified API contract drift.
[ ] AI identified test coverage gaps.
[ ] AI identified documentation drift.
[ ] AI provided recommended fix order.
[ ] AI gave a Go/No-go recommendation.
[ ] AI did not make broad unreviewed changes.
```

## Review Checklist for Author

After Codex completes the review, verify:

```text id="m6mmuz"
[ ] Did the AI provide evidence for each claim?
[ ] Did it mark unverified items as Not verified?
[ ] Did it avoid claiming tests passed without running them?
[ ] Did it classify financial defects as Critical?
[ ] Did it classify settlement atomicity defects as Critical?
[ ] Did it classify missing AI_USAGE transparency appropriately?
[ ] Did it avoid suggesting broad rewrites unnecessarily?
[ ] Did it provide a practical fix order?
[ ] Are the suggested fixes aligned with specs and ADRs?
```

## Common AI Review Mistakes to Reject

Reject or correct the review if the AI:

```text id="pih8h3"
- says everything is ready without inspecting files;
- claims tests passed without running commands;
- ignores BigDecimal/double risks;
- ignores frontend formula duplication;
- ignores settlement atomicity;
- ignores statement in-memory filtering;
- ignores AI_USAGE.md;
- suggests adding microservices, Kafka or Kubernetes as required fixes;
- proposes broad rewrites instead of targeted fixes;
- fails to separate Critical, High and Medium issues;
- does not reference specs or ADRs;
- does not mark unverifiable items as Not verified.
```

## Suggested PR Description for Review Fixes

````md id="6hhxls"
## Summary

Addresses final review findings before technical challenge delivery.

## Specs Covered

- docs/specs/08-acceptance-criteria.md
- docs/specs/11-delivery-checklist.md
- docs/adr/ADR-007-ai-assisted-development.md

## Changes

- Fixed final review issue <ID>.
- Updated tests for <behavior>.
- Updated documentation where needed.

## Tests

- [x] Backend tests run
- [x] Frontend build run
- [x] Docker validation run, if applicable
- [x] Manual validation performed

Commands executed:

```bash
<commands>
````

## Risks and Trade-offs

* <remaining risk or none>

## AI Usage

AI was used for:

* final review support
* targeted fix support

AI_USAGE.md:

* [ ] Updated
* [ ] Not applicable

````id="txq90w"

## Related Documents

```text
AGENTS.md
README.md
AI_USAGE.md
docker-compose.yml
.env.example
docs/specs/00-spec-index.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/specs/08-acceptance-criteria.md
docs/specs/09-git-workflow.md
docs/specs/10-ai-workflow.md
docs/specs/11-delivery-checklist.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-005-frontend-stack.md
docs/adr/ADR-006-git-workflow.md
docs/adr/ADR-007-ai-assisted-development.md
docs/diagrams/er-diagram.md
docs/diagrams/c4-context.md
docs/diagrams/c4-container.md
````

