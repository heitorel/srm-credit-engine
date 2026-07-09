# 11 - Delivery Checklist

## 1. Purpose

This document defines the final delivery checklist for the **SRM Credit Engine** technical challenge.

The checklist must be used before merging `develop` into `main` and creating the final release tag.

The goal is to verify that the project is complete, runnable, documented, testable and aligned with the expected Pleno-level delivery.

## 2. Delivery Target

Target seniority level:

```text
Pleno / Mid-Level Software Engineer
```

The delivery must demonstrate:

* backend functionality;
* frontend functionality;
* financial-domain correctness;
* decimal-safe calculations;
* ACID settlement behavior;
* Strategy Pattern usage;
* relational data modeling;
* Docker Compose execution;
* API documentation;
* automated tests;
* organized Git workflow;
* transparent AI usage.

Optional senior-level differentials may be included only if they do not compromise the required Pleno-level scope.

## 3. Final Delivery Summary

The final repository must include:

```text
1. Spring Boot backend
2. Angular frontend
3. MySQL database
4. Flyway migrations
5. Docker Compose
6. OpenAPI/Swagger documentation
7. Unit and integration tests
8. ER diagram
9. C4 context and container diagrams
10. README with setup and architecture documentation
11. AI_USAGE.md
12. AGENTS.md
13. Specs and ADRs
14. Prompt files for AI-assisted development
15. Clean Git history
16. Final release tag
```

## 4. Repository Structure Checklist

Verify that the repository contains:

```text
srm-credit-engine/
|-- AGENTS.md
|-- AI_USAGE.md
|-- README.md
|-- docker-compose.yml
|-- .gitignore
|-- .editorconfig
|-- .env.example
|-- docs/
|   |-- specs/
|   |-- diagrams/
|   |-- adr/
|   `-- prompts/
|-- backend/
`-- frontend/
```

Checklist:

```text
[ ] Root files exist.
[ ] docs/specs contains all planned specification files.
[ ] docs/adr contains all planned ADR files.
[ ] docs/diagrams contains ER and C4 diagrams.
[ ] docs/prompts contains reusable Codex prompts.
[ ] backend directory contains Spring Boot project.
[ ] frontend directory contains Angular project.
[ ] No unexpected large/generated files are committed.
[ ] No local environment files are committed.
```

## 5. Root Configuration Checklist

## 5.1 `.gitignore`

```text
[ ] Ignores .env files.
[ ] Ignores IDE files.
[ ] Ignores Java build artifacts.
[ ] Ignores Node/Angular build artifacts.
[ ] Ignores logs.
[ ] Ignores Docker local volume folders.
[ ] Does not ignore required documentation files.
```

## 5.2 `.editorconfig`

```text
[ ] Defines UTF-8 charset.
[ ] Defines LF line endings.
[ ] Inserts final newline.
[ ] Trims trailing whitespace where appropriate.
[ ] Defines Java indentation.
[ ] Defines TypeScript/Angular indentation.
[ ] Defines YAML/JSON/Markdown indentation.
```

## 5.3 `.env.example`

```text
[ ] Contains safe example values only.
[ ] Documents backend port.
[ ] Documents frontend port.
[ ] Documents MySQL database configuration.
[ ] Documents Spring datasource variables.
[ ] Documents CORS allowed origins.
[ ] Documents Angular API base URL.
[ ] Does not contain real secrets.
```

## 5.4 `docker-compose.yml`

```text
[ ] Defines MySQL service.
[ ] Defines backend service.
[ ] Defines frontend service.
[ ] MySQL uses version 8.4 LTS.
[ ] MySQL has healthcheck.
[ ] Backend depends on healthy MySQL.
[ ] Backend receives datasource environment variables.
[ ] Frontend exposes port 4200.
[ ] Docker network is configured.
[ ] Docker volume is configured for MySQL.
[ ] `docker compose config` succeeds.
```

## 6. Backend Checklist

## 6.1 Backend Scaffold

```text
[ ] Backend project exists under backend/.
[ ] Backend uses Java 21.
[ ] Backend uses Spring Boot 4.1.x.
[ ] Backend uses Maven.
[ ] Backend has correct base package.
[ ] Backend has application entry point.
[ ] Backend has application configuration files.
[ ] Backend has Dockerfile.
[ ] Backend starts locally.
```

Expected command:

```bash
cd backend
mvn spring-boot:run
```

Wrapper equivalent:

```bash
cd backend
./mvnw spring-boot:run
```

## 6.2 Backend Dependencies

Verify backend includes required dependencies:

```text
[ ] Spring Web.
[ ] Spring Validation.
[ ] Spring Data JPA.
[ ] MySQL JDBC driver.
[ ] Flyway.
[ ] OpenAPI/Swagger dependency.
[ ] Spring Boot Test.
[ ] JUnit 5.
[ ] Mockito.
[ ] AssertJ.
[ ] Testcontainers, if integration tests require MySQL.
```

## 6.3 Backend Layering

```text
[ ] API layer exists.
[ ] Application layer exists.
[ ] Domain layer exists.
[ ] Infrastructure layer exists.
[ ] Controllers are thin.
[ ] Business logic is not in controllers.
[ ] Transaction orchestration is in application services.
[ ] Pricing rules are in domain services/strategies.
[ ] Persistence details are in infrastructure.
```

Expected structure:

```text
api
application
domain
infrastructure
```

## 6.4 API Endpoints

Required endpoints:

```text
[ ] GET  /api/reference-data/currencies
[ ] GET  /api/reference-data/receivable-types
[ ] POST /api/exchange-rates
[ ] GET  /api/exchange-rates/latest
[ ] POST /api/pricing/simulations
[ ] POST /api/settlements
[ ] GET  /api/settlements/{id}
[ ] GET  /api/settlements/statement
```

Optional endpoint:

```text
[ ] GET /actuator/health
```

## 6.5 OpenAPI / Swagger

```text
[ ] Swagger UI is available.
[ ] OpenAPI JSON is available.
[ ] Main endpoints are documented.
[ ] Request DTOs are visible.
[ ] Response DTOs are visible.
[ ] Error responses are documented where useful.
[ ] Monetary examples use decimal rate format.
```

Expected URLs:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

or equivalent Springdoc paths.

## 6.6 Validation

Backend validation must cover:

```text
[ ] Required fields.
[ ] Positive face value.
[ ] Base rate greater than or equal to zero when provided.
[ ] Effective base rate is resolved from request first, then DEFAULT_BASE_RATE.
[ ] Missing request base rate and missing DEFAULT_BASE_RATE fails clearly.
[ ] Positive exchange rate.
[ ] Supported source currency.
[ ] Supported target currency.
[ ] Supported payment currency.
[ ] Source and target currencies are different for exchange rates.
[ ] Supported receivable type.
[ ] Future due date.
[ ] Settlement batch size between 1 and 100.
[ ] One source currency per settlement batch.
[ ] Valid pagination parameters.
[ ] Valid statement date range.
```

## 6.7 Error Handling

```text
[ ] Global exception handler exists.
[ ] Validation errors return structured responses.
[ ] Business errors return structured responses.
[ ] Unexpected errors return controlled responses.
[ ] Stack traces are not exposed through API responses.
[ ] Duplicate settlement returns 409 Conflict.
[ ] Missing exchange rate returns 422 Unprocessable Entity.
[ ] Unknown settlement returns 404 Not Found.
```

Expected error fields:

```text
timestamp
status
error
message
path
details
```

## 6.8 Financial Calculation

```text
[ ] Financial calculations use BigDecimal.
[ ] Financial calculations do not use double.
[ ] Financial calculations do not use float.
[ ] BigDecimal constants are not created from floating-point literals.
[ ] Effective base rate resolution order is explicit.
[ ] Monetary scale is explicit.
[ ] Rate scale is explicit.
[ ] Rounding mode is explicit.
[ ] Present value formula is implemented.
[ ] Discount is calculated as face value minus present value.
[ ] Cross-currency conversion happens after present value calculation.
[ ] Same-currency operations do not require exchange rate.
[ ] Missing exchange rate fails cross-currency pricing.
```

Critical code review checks:

```text
[ ] No `double` in financial domain code.
[ ] No `float` in financial domain code.
[ ] No `Math.pow` used with double-based financial calculation unless explicitly wrapped, documented and tested.
[ ] No `new BigDecimal(0.015)`.
[ ] No `new BigDecimal(0.025)`.
```

## 6.9 Pricing Strategy

```text
[ ] PricingStrategy abstraction exists.
[ ] MercantileDuplicatePricingStrategy exists.
[ ] PostDatedCheckPricingStrategy exists.
[ ] PricingStrategyResolver exists.
[ ] MERCANTILE_DUPLICATE uses spread 0.01500000.
[ ] POST_DATED_CHECK uses spread 0.02500000.
[ ] Unsupported receivable type fails clearly.
[ ] Strategy behavior is unit tested.
```

## 6.10 Currency Engine

```text
[ ] Exchange rate can be created.
[ ] Exchange rate rate must be positive.
[ ] Source and target currencies must differ.
[ ] Latest exchange rate can be retrieved.
[ ] Exchange-rate direction is explicit.
[ ] Inverse pair is not silently inferred.
[ ] Same-currency operation does not require exchange-rate lookup.
```

## 6.11 Settlement Engine

```text
[ ] Settlement creation endpoint exists.
[ ] Settlement accepts a batch of receivables.
[ ] Empty batch is rejected.
[ ] Batch size above 100 is rejected.
[ ] Mixed source currency batch is rejected.
[ ] Settlement response includes header source currency.
[ ] Settlement creation is transactional.
[ ] Settlement header is persisted.
[ ] Settlement items are persisted.
[ ] Receivables are persisted or resolved.
[ ] Receivable status changes to SETTLED after settlement.
[ ] Duplicate settlement is prevented.
[ ] Settlement response includes audit snapshots.
[ ] Failed settlement does not leave partial records.
```

## 6.12 Settlement Auditability

Each settlement item must persist:

```text
[ ] receivable id.
[ ] external reference.
[ ] receivable type.
[ ] face value.
[ ] source currency.
[ ] payment currency.
[ ] base rate.
[ ] spread.
[ ] term in months.
[ ] present value in source currency.
[ ] discount value.
[ ] payment value.
[ ] exchange rate when applicable.
[ ] calculation timestamp.
```

Historical integrity:

```text
[ ] Settlement detail uses persisted values.
[ ] Statement query uses persisted values.
[ ] Old settlement values do not change when new exchange rates are inserted.
```

## 6.13 Settlement Statement

```text
[ ] Statement endpoint exists.
[ ] Supports period filter.
[ ] Supports assignor filter.
[ ] Supports payment currency filter.
[ ] Supports source currency filter.
[ ] Supports receivable type filter.
[ ] Supports status filter.
[ ] Uses server-side pagination.
[ ] Uses database-level filtering.
[ ] Does not use findAll plus in-memory filtering.
[ ] Sorts by settledAt descending by default.
[ ] Enforces maximum page size.
```

## 7. Database Checklist

## 7.1 Flyway

```text
[ ] Flyway is enabled.
[ ] Migrations exist under backend/src/main/resources/db/migration.
[ ] V1__create_initial_schema.sql exists.
[ ] V2__seed_reference_data.sql exists.
[ ] Migrations run from empty database.
[ ] Hibernate validates schema after migration.
```

## 7.2 Required Tables

```text
[ ] currencies
[ ] receivable_types
[ ] assignors
[ ] exchange_rates
[ ] receivables
[ ] settlements
[ ] settlement_items
```

## 7.3 Required Seed Data

Currencies:

```text
[ ] BRL
[ ] USD
```

Receivable types:

```text
[ ] MERCANTILE_DUPLICATE with monthly_spread 0.01500000
[ ] POST_DATED_CHECK with monthly_spread 0.02500000
```

## 7.4 Financial Columns

```text
[ ] Monetary columns use DECIMAL.
[ ] Rate columns use DECIMAL.
[ ] Exchange-rate columns use DECIMAL.
[ ] Term columns use DECIMAL.
[ ] No financial column uses FLOAT.
[ ] No financial column uses DOUBLE.
[ ] No financial column uses REAL.
```

## 7.5 Constraints

```text
[ ] Primary keys exist.
[ ] Foreign keys exist.
[ ] Exchange rate must be positive.
[ ] Face value must be positive.
[ ] Base rate must be non-negative.
[ ] Spread must be non-negative.
[ ] Term must be positive.
[ ] Receivable external reference is unique per assignor.
[ ] Settlement item has unique receivable_id.
[ ] Status values are constrained or validated.
```

## 7.6 Indexes

Required index support for:

```text
[ ] exchange rate pair and valid_at.
[ ] receivable assignor.
[ ] receivable type.
[ ] receivable currency.
[ ] receivable status.
[ ] settlement assignor.
[ ] settlement source currency.
[ ] settlement payment currency.
[ ] settlement status.
[ ] settlement settled_at.
[ ] settlement item settlement_id.
[ ] settlement item receivable type.
[ ] settlement item source currency.
[ ] settlement item payment currency.
```

## 8. Frontend Checklist

## 8.1 Frontend Scaffold

```text
[ ] Frontend project exists under frontend/.
[ ] Frontend uses Angular 22.
[ ] Frontend uses TypeScript.
[ ] Angular Material is configured.
[ ] Angular routing is configured.
[ ] Frontend has Dockerfile.
[ ] Frontend builds successfully.
```

Expected command:

```bash
cd frontend
npm run build
```

## 8.2 Frontend Structure

Expected structure or equivalent:

```text
[ ] core/
[ ] shared/
[ ] features/
[ ] models/
```

Feature areas:

```text
[ ] pricing-simulation
[ ] settlements
[ ] exchange-rates, if implemented in UI
```

## 8.3 Pricing Simulation Screen

```text
[ ] Screen exists.
[ ] Uses Reactive Forms.
[ ] Collects face value.
[ ] Collects source currency.
[ ] Collects payment currency.
[ ] Collects base rate.
[ ] Collects receivable type.
[ ] Collects due date.
[ ] Calls POST /api/pricing/simulations.
[ ] Displays present value.
[ ] Displays discount value.
[ ] Displays net payment value.
[ ] Displays exchange rate when applicable.
[ ] Displays validation errors.
[ ] Displays backend business errors.
[ ] Does not implement official pricing formula.
[ ] Does not hardcode spreads.
[ ] Does not hardcode exchange rates.
```

## 8.4 Settlement Statement Grid

```text
[ ] Screen exists.
[ ] Calls GET /api/settlements/statement.
[ ] Displays settlement rows.
[ ] Supports date filters.
[ ] Supports currency filters.
[ ] Supports receivable type filter.
[ ] Supports pagination.
[ ] Uses server-side pagination.
[ ] Does not load all rows and paginate locally.
[ ] Displays loading state.
[ ] Displays empty state.
[ ] Displays error state.
```

## 8.5 Exchange Rate UI

If implemented:

```text
[ ] Exchange-rate form exists.
[ ] Collects source currency.
[ ] Collects target currency.
[ ] Collects rate.
[ ] Collects validAt.
[ ] Calls POST /api/exchange-rates.
[ ] Shows success state.
[ ] Shows validation/business errors.
```

If not implemented:

```text
[ ] README explains exchange rates can be created through Swagger/API.
```

## 8.6 Frontend API Models

```text
[ ] PricingSimulationRequest exists.
[ ] PricingSimulationResponse exists.
[ ] ExchangeRateRequest/Response exists if used.
[ ] SettlementStatementResponse exists.
[ ] PageResponse model exists.
[ ] ApiErrorResponse model exists.
[ ] Models match docs/specs/04-api-contract.md.
```

## 8.7 Frontend Error Handling

```text
[ ] Field validation errors are visible.
[ ] Backend validation errors are visible.
[ ] Backend business errors are visible.
[ ] Unexpected errors show fallback message.
[ ] Operator can understand missing exchange rate errors.
[ ] Operator can understand invalid due date errors.
```

## 8.8 Frontend Monetary Display

```text
[ ] Monetary values include currency context.
[ ] Source currency is visible.
[ ] Payment currency is visible.
[ ] Cross-currency values are not ambiguous.
[ ] Values are formatted consistently.
```

## 9. Testing Checklist

## 9.1 Backend Test Commands

Verify:

```bash
cd backend
mvn test
```

Wrapper equivalent:

```bash
cd backend
./mvnw test
```

If integration tests are configured under `verify`:

```bash
cd backend
mvn verify
```

Wrapper equivalent:

```bash
cd backend
./mvnw verify
```

Checklist:

```text
[ ] Backend compiles.
[ ] Unit tests pass.
[ ] Integration tests pass, if configured.
[ ] Test failures are not ignored.
```

## 9.2 Required Backend Unit Tests

```text
[ ] Mercantile Duplicate strategy test.
[ ] Post-Dated Check strategy test.
[ ] Strategy resolver test.
[ ] Same-currency pricing test.
[ ] Cross-currency pricing test.
[ ] Cross-currency conversion order test.
[ ] Configured base-rate fallback test.
[ ] Request base-rate precedence-over-fallback test.
[ ] Missing exchange-rate test.
[ ] Invalid due-date test.
[ ] Discount calculation test.
[ ] Rounding behavior test.
```

## 9.3 Required Backend Integration/API Tests

```text
[ ] Flyway migration test or startup validation.
[ ] Exchange-rate persistence/retrieval test.
[ ] Settlement creation test.
[ ] Settlement rollback/atomicity test.
[ ] Duplicate settlement prevention test.
[ ] Settlement detail audit snapshot test.
[ ] Statement query filter test.
[ ] Statement query pagination test.
[ ] API validation error test.
[ ] Structured error response test.
```

## 9.4 Frontend Test Commands

If frontend tests are configured:

```bash
cd frontend
npm test
```

Build validation:

```bash
cd frontend
npm run build
```

Checklist:

```text
[ ] Frontend builds.
[ ] Frontend tests pass, if included.
[ ] Frontend does not have blocking runtime errors.
```

## 9.5 Manual Validation

Manual validation must cover:

```text
[ ] Docker Compose starts all services.
[ ] Swagger opens.
[ ] Reference data endpoints work.
[ ] Exchange rate can be created.
[ ] Latest exchange rate can be retrieved.
[ ] Same-currency pricing simulation works.
[ ] Cross-currency pricing simulation works.
[ ] Request base rate overrides configured fallback.
[ ] Configured base-rate fallback works when request base rate is omitted.
[ ] Missing exchange rate fails clearly.
[ ] Settlement batch can be created.
[ ] Duplicate settlement fails.
[ ] Settlement detail shows audit values.
[ ] Statement query filters results.
[ ] Statement query paginates results.
[ ] Angular frontend opens.
[ ] Angular simulation screen calls backend.
[ ] Angular settlement grid calls backend.
```

## 10. Documentation Checklist

## 10.1 README

README must include:

```text
[ ] Project overview.
[ ] Business context.
[ ] Stack.
[ ] Architecture summary.
[ ] Prerequisites.
[ ] Environment variables.
[ ] Docker Compose instructions.
[ ] Backend local run instructions.
[ ] Frontend local run instructions.
[ ] Test commands.
[ ] Swagger URL.
[ ] Frontend URL.
[ ] Database/migration explanation.
[ ] Business assumptions.
[ ] Git workflow.
[ ] AI usage reference.
[ ] Known limitations.
[ ] Future improvements.
```

## 10.2 Specs

Verify all files are complete:

```text
[ ] docs/specs/00-spec-index.md
[ ] docs/specs/01-product-brief.md
[ ] docs/specs/02-domain-glossary.md
[ ] docs/specs/03-business-rules.md
[ ] docs/specs/04-api-contract.md
[ ] docs/specs/05-data-model.md
[ ] docs/specs/06-architecture.md
[ ] docs/specs/07-testing-strategy.md
[ ] docs/specs/08-acceptance-criteria.md
[ ] docs/specs/09-git-workflow.md
[ ] docs/specs/10-ai-workflow.md
[ ] docs/specs/11-delivery-checklist.md
```

## 10.3 ADRs

Verify all ADRs are complete:

```text
[ ] docs/adr/ADR-001-backend-stack.md
[ ] docs/adr/ADR-002-database-choice.md
[ ] docs/adr/ADR-003-money-precision.md
[ ] docs/adr/ADR-004-architecture-style.md
[ ] docs/adr/ADR-005-frontend-stack.md
[ ] docs/adr/ADR-006-git-workflow.md
[ ] docs/adr/ADR-007-ai-assisted-development.md
```

## 10.4 Diagrams

Verify diagrams exist and match implementation:

```text
[ ] docs/diagrams/er-diagram.md
[ ] docs/diagrams/c4-context.md
[ ] docs/diagrams/c4-container.md
```

Diagram consistency:

```text
[ ] ER diagram matches Flyway migrations.
[ ] C4 context diagram matches system boundaries.
[ ] C4 container diagram matches Docker/runtime architecture.
```

## 10.5 Prompts

Verify prompt files exist:

```text
[ ] docs/prompts/00-bootstrap.md
[ ] docs/prompts/01-backend-scaffold.md
[ ] docs/prompts/02-database-migrations.md
[ ] docs/prompts/03-currency-engine.md
[ ] docs/prompts/04-pricing-engine.md
[ ] docs/prompts/05-settlement-flow.md
[ ] docs/prompts/06-settlement-detail.md
[ ] docs/prompts/07-statement-query.md
[ ] docs/prompts/08-frontend-scaffold.md
[ ] docs/prompts/09-frontend-simulation.md
[ ] docs/prompts/10-frontend-statement-grid.md
[ ] docs/prompts/11-frontend-exchange-rates.md
[ ] docs/prompts/12-docker-and-delivery.md
[ ] docs/prompts/13-review.md
```

Prompt quality:

```text
[ ] Prompts reference relevant specs.
[ ] Prompts reference relevant ADRs.
[ ] Prompts define allowed scope.
[ ] Prompts define forbidden changes.
[ ] Prompts request tests.
[ ] Prompts include financial safety constraints.
```

## 11. AI Usage Checklist

`AI_USAGE.md` must include:

```text
[ ] Purpose.
[ ] AI usage policy.
[ ] Tools used.
[ ] Planning-stage entries.
[ ] Specification-stage entries.
[ ] Implementation-stage entries, if AI was used.
[ ] Testing/review entries, if AI was used.
[ ] Rejected or corrected AI output.
[ ] Critical analysis of where AI helped.
[ ] Critical analysis of where AI was risky.
[ ] Final ownership statement.
```

For each meaningful AI interaction:

```text
[ ] Tool used.
[ ] Prompt summary.
[ ] AI contribution.
[ ] Author review.
[ ] Accepted changes.
[ ] Rejected or corrected output.
[ ] Tests or validation performed.
[ ] Known limitations.
```

Critical AI risks reviewed:

```text
[ ] No AI-generated floating-point financial calculation accepted.
[ ] No AI-generated wrong spread representation accepted.
[ ] No AI-generated wrong currency conversion order accepted.
[ ] No AI-generated in-memory statement filtering accepted.
[ ] No AI-generated frontend financial formula accepted.
```

## 12. Git Checklist

## 12.1 Branches

```text
[ ] main exists.
[ ] develop exists.
[ ] feature branches were created from develop.
[ ] feature branches were merged into develop.
[ ] final develop was merged into main.
```

## 12.2 Commits

```text
[ ] Initial specification commit exists.
[ ] Commit messages follow Conventional Commits after baseline.
[ ] Commits are readable.
[ ] Commits are scoped.
[ ] No single implementation commit contains the entire project.
[ ] No vague messages such as "finalizado" or "ajustes".
```

## 12.3 Pull Requests

```text
[ ] Simulated PRs exist or are represented in repository history/platform.
[ ] PR descriptions include summary.
[ ] PR descriptions include specs covered.
[ ] PR descriptions include tests run.
[ ] PR descriptions include risks/trade-offs.
[ ] PR descriptions mention AI usage when material.
```

## 12.4 Release

```text
[ ] develop is merged into main.
[ ] Final tag v1.0.0 exists.
[ ] Release notes exist if using GitHub/GitLab releases.
```

Expected tag:

```text
v1.0.0
```

## 13. Security Checklist

```text
[ ] No real secrets committed.
[ ] .env is ignored.
[ ] .env.example contains safe values only.
[ ] API responses do not expose stack traces.
[ ] Backend validation exists for all public inputs.
[ ] CORS is explicit.
[ ] SQL queries do not concatenate unsafe user input.
[ ] Logs do not expose credentials.
[ ] Docker Compose does not require production secrets.
```

## 14. Performance Checklist

```text
[ ] Statement query uses database-level filtering.
[ ] Statement query uses server-side pagination.
[ ] Statement query has supporting indexes.
[ ] Batch size is limited.
[ ] Frontend does not load all statement rows.
[ ] Frontend does not paginate locally.
[ ] Reference data may be cached in frontend if useful, but not required.
```

## 15. Observability Checklist

Minimum:

```text
[ ] Backend logs application startup.
[ ] Backend logs exchange-rate creation.
[ ] Backend logs settlement creation.
[ ] Backend logs settlement failure.
[ ] Backend logs unexpected errors.
[ ] Logs do not expose secrets.
```

Optional:

```text
[ ] Structured logs.
[ ] Correlation ID.
[ ] Basic metrics.
[ ] Health endpoint.
```

## 16. Known Limitations Checklist

The final README must document known limitations.

Expected limitations may include:

```text
[ ] Authentication is out of scope.
[ ] Authorization is out of scope.
[ ] DEFAULT_BASE_RATE fallback is configuration-dependent.
[ ] Real exchange-rate provider integration is out of scope.
[ ] Real banking settlement execution is out of scope.
[ ] Mixed-source-currency settlement batches are out of scope.
[ ] Production cloud deployment is out of scope.
[ ] Kubernetes is out of scope.
[ ] Advanced observability is optional.
```

## 17. Future Improvements Checklist

The final README or architecture docs should mention future improvements such as:

```text
[ ] Authentication and authorization.
[ ] Base-rate management module.
[ ] Real exchange-rate provider integration.
[ ] Approval workflow.
[ ] Export settlement statements.
[ ] Outbox pattern.
[ ] Event-driven settlement processing.
[ ] Read replica for reporting.
[ ] Caching reference data.
[ ] Advanced observability.
[ ] High-scale architecture.
```

## 18. Final Command Checklist

Before final merge, run or validate equivalent commands.

Backend:

```bash
cd backend
mvn test
```

Wrapper equivalent:

```bash
cd backend
./mvnw test
```

Optional full backend verification:

```bash
cd backend
mvn verify
```

Wrapper equivalent:

```bash
cd backend
./mvnw verify
```

Frontend:

```bash
cd frontend
npm run build
```

Optional frontend tests:

```bash
cd frontend
npm test
```

Docker Compose:

```bash
docker compose config
docker compose up --build
```

Git:

```bash
git status
git log --oneline --decorate --graph --all
git tag
```

## 19. Final Manual Smoke Test

After `docker compose up --build`, verify:

```text
[ ] Frontend opens at http://localhost:4200.
[ ] Backend responds at http://localhost:8080.
[ ] Swagger opens.
[ ] MySQL container is healthy.
[ ] Reference data endpoints work.
[ ] Exchange rate creation works.
[ ] Pricing simulation works.
[ ] Settlement creation works.
[ ] Settlement detail works.
[ ] Statement query works.
```

## 20. Minimum Required Delivery Criteria

The project must not be delivered if any of these are false:

```text
[ ] Backend does not start.
[ ] Frontend does not start.
[ ] Docker Compose does not work.
[ ] Flyway migrations do not run.
[ ] Pricing strategy tests are missing.
[ ] Financial calculation uses floating-point types.
[ ] Cross-currency conversion order is wrong.
[ ] Settlement is not atomic.
[ ] Settlement snapshots are not persisted.
[ ] Duplicate settlement is not prevented.
[ ] Statement query uses in-memory filtering.
[ ] README lacks setup instructions.
[ ] AI_USAGE.md is missing or superficial.
[ ] Git history is a single unstructured final commit.
```

## 21. Final Release Checklist

Before tagging:

```text
[ ] All critical acceptance criteria are satisfied.
[ ] High-priority criteria are satisfied or justified.
[ ] Tests pass.
[ ] Docker Compose runs.
[ ] README is final.
[ ] AI_USAGE.md is final.
[ ] Specs match implementation.
[ ] ADRs match implementation.
[ ] Diagrams match implementation.
[ ] No secrets are committed.
[ ] Git history is readable.
[ ] develop is ready to merge into main.
```

Release commands:

```bash
git checkout main
git merge --no-ff develop
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin main
git push origin v1.0.0
```

## 22. Final Delivery Statement

The final project is ready for delivery when the following statement is true:

```text
The SRM Credit Engine can be run locally, demonstrates financial-domain correctness, uses decimal-safe calculations, settles receivable batches atomically, persists auditable calculation snapshots, exposes documented REST APIs, provides an Angular operator frontend, includes meaningful automated tests, documents AI usage transparently and presents a professional Git history with a final release tag.
```

## 23. Related Documents

```text
README.md
AGENTS.md
AI_USAGE.md
docker-compose.yml
.env.example
docs/specs/00-spec-index.md
docs/specs/01-product-brief.md
docs/specs/02-domain-glossary.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/specs/08-acceptance-criteria.md
docs/specs/09-git-workflow.md
docs/specs/10-ai-workflow.md
docs/adr/*
docs/diagrams/*
docs/prompts/*
```
