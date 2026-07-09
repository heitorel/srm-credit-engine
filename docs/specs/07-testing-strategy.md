# 07 - Testing Strategy

## 1. Purpose

This document defines the testing strategy for the **SRM Credit Engine**.

The goal is to verify that the system behaves correctly in the areas that matter most for a financial technical challenge:

* monetary precision;
* pricing rules;
* Strategy Pattern behavior;
* exchange-rate handling;
* cross-currency conversion;
* settlement atomicity;
* duplicate settlement prevention;
* API validation;
* structured error handling;
* analytical query filtering and pagination;
* frontend integration with backend contracts.

Tests must validate business behavior, not only implementation details.

## 2. Testing Principles

The project follows these testing principles:

1. Business-critical logic must be covered by automated tests.
2. Financial calculations must be deterministic and explicit.
3. Tests must protect against floating-point financial calculations.
4. Domain tests should be fast and independent from Spring Boot when possible.
5. Persistence behavior must be tested with MySQL-compatible integration tests when relevant.
6. API tests must verify validation, status codes and error responses.
7. Frontend tests should focus on forms, API integration and pagination behavior.
8. AI-generated code must not be accepted without tests when it touches business logic.
9. Tests should be readable enough to document expected behavior.
10. The final delivery must include clear commands for running tests.

## 3. Testing Scope

The test suite is divided into the following levels:

```text id="zbt3wi"
1. Domain unit tests
2. Application service tests
3. Infrastructure integration tests
4. API/controller tests
5. Frontend tests
6. End-to-end/manual validation
```

The highest priority is backend domain and application behavior.

## 4. Tooling

## 4.1 Backend Test Tools

Selected tools:

```text id="5t97iu"
JUnit 5
Mockito
AssertJ
Spring Boot Test
Testcontainers
MySQL Testcontainers
MockMvc or WebTestClient
```

Expected use:

| Tool                  | Purpose                                      |
| --------------------- | -------------------------------------------- |
| JUnit 5               | Test runner and lifecycle                    |
| Mockito               | Mock collaborators in unit tests             |
| AssertJ               | Fluent assertions                            |
| Spring Boot Test      | Application/context integration tests        |
| Testcontainers        | Database-backed integration tests            |
| MySQL Testcontainers  | Validate MySQL-specific persistence behavior |
| MockMvc/WebTestClient | API/controller contract tests                |

## 4.2 Frontend Test Tools

Frontend testing should use Angular ecosystem defaults unless implementation selects a documented alternative.

Expected tools:

```text id="pymdm7"
Angular TestBed
Jasmine or equivalent Angular default
Karma or Angular CLI default runner
HttpClientTestingModule or equivalent HTTP testing utilities
```

If the implementation uses a different Angular test runner, such as Vitest, the change must be documented.

## 4.3 Docker Validation

Docker validation is not a substitute for automated tests, but the final delivery should verify:

```bash id="hh5td8"
docker compose up --build
```

Expected result:

* MySQL starts;
* backend starts;
* Flyway migrations run;
* frontend starts;
* Swagger is accessible;
* frontend can call backend API.

## 5. Test Pyramid

The intended testing distribution is:

```text id="s36cyd"
Many domain unit tests
Some application/integration tests
Focused API tests
Focused frontend tests
Few manual end-to-end checks
```

The project should not rely mainly on manual testing.

## 6. Domain Unit Tests

## 6.1 Purpose

Domain unit tests verify the core financial rules without requiring:

* Spring Boot context;
* database;
* HTTP layer;
* frontend.

These tests should be fast, deterministic and easy to read.

## 6.2 Target Packages

Expected target packages:

```text id="mqssuu"
domain.currency
domain.exchange
domain.pricing
domain.receivable
domain.settlement
domain.shared
```

## 6.3 Required Domain Test Areas

### 6.3.1 Money Value Object

Test:

* amount cannot be null;
* currency cannot be null;
* money cannot use unsupported currency;
* money arithmetic preserves currency;
* invalid operations between different currencies fail;
* monetary rounding follows project policy.

Example test names:

```text id="6yxwym"
shouldCreateMoneyWithValidAmountAndCurrency
shouldRejectNullAmount
shouldRejectNullCurrency
shouldRejectAddingDifferentCurrencies
shouldRoundMoneyUsingConfiguredScale
```

### 6.3.2 Rate Value Object

Test:

* rate cannot be null;
* base rate can be zero;
* exchange rate must be greater than zero;
* spread values are represented as decimals;
* `1.5%` is represented as `0.01500000`;
* `2.5%` is represented as `0.02500000`.

Example test names:

```text id="xuffuv"
shouldCreateBaseRateWithZeroValue
shouldRejectNegativeBaseRate
shouldRejectZeroExchangeRate
shouldRepresentMercantileDuplicateSpreadAsDecimal
shouldRepresentPostDatedCheckSpreadAsDecimal
```

### 6.3.3 Term Calculation

Test:

* future due date generates positive term;
* past due date is rejected;
* same-day due date is rejected in the initial version;
* term uses commercial-month convention;
* fixed clock makes tests deterministic.

Example test names:

```text id="kwjb42"
shouldCalculateCommercialMonthTermFromDueDate
shouldRejectPastDueDate
shouldRejectSameDayDueDate
shouldUseFixedClockForDeterministicTermCalculation
```

### 6.3.4 Pricing Strategy

Strategy tests are mandatory.

Test:

* `MERCANTILE_DUPLICATE` strategy returns `0.01500000`;
* `POST_DATED_CHECK` strategy returns `0.02500000`;
* unsupported receivable type fails clearly;
* resolver returns correct strategy;
* strategy classes do not require persistence.

Example test names:

```text id="uqxhhg"
shouldResolveMercantileDuplicateStrategy
shouldResolvePostDatedCheckStrategy
shouldReturnMercantileDuplicateSpread
shouldReturnPostDatedCheckSpread
shouldFailForUnsupportedReceivableType
```

### 6.3.5 Pricing Engine

Test:

* present value calculation for same-currency operation;
* discount calculation;
* payment value equals present value for same-currency operation;
* cross-currency conversion is applied after present value calculation;
* configured server default base rate is used when request base rate is omitted;
* request base rate overrides configured server default;
* missing request base rate and missing configured default fails clearly;
* missing exchange rate fails when cross-currency;
* no exchange rate is required when same-currency;
* rounding behavior is explicit;
* final result contains all audit-relevant fields.

Example test names:

```text id="cnku4q"
shouldCalculatePresentValueForSameCurrencyOperation
shouldCalculateDiscountAsFaceValueMinusPresentValue
shouldUsePresentValueAsPaymentValueWhenSameCurrency
shouldApplyExchangeConversionAfterPresentValueCalculation
shouldFailCrossCurrencyPricingWhenExchangeRateIsMissing
shouldNotRequireExchangeRateForSameCurrencyPricing
shouldReturnAuditFieldsInPricingResult
```

### 6.3.6 Currency Conversion Service

Test:

* converts source amount to target amount using explicit rate;
* rejects missing rate for cross-currency operation;
* does not silently invert exchange-rate direction;
* preserves scale and rounding policy.

Example test names:

```text id="3za13h"
shouldConvertUsingExplicitExchangeRate
shouldRejectMissingExchangeRate
shouldNotInvertExchangeRateAutomatically
shouldRoundConvertedAmountUsingConfiguredPolicy
```

## 7. Application Service Tests

## 7.1 Purpose

Application tests verify use case orchestration.

They may use mocks for repositories and domain services when testing orchestration only.

They may use Spring context when transaction behavior or integration is relevant.

## 7.2 Target Use Cases

Expected application services:

```text id="2q462u"
CreateExchangeRateService
GetLatestExchangeRateService
PricingSimulationService
CreateSettlementService
GetSettlementService
SettlementStatementService
ListReferenceDataService
```

## 7.3 Pricing Simulation Service Tests

Test:

* valid request returns pricing response;
* same domain pricing logic is used;
* simulation does not persist settlement;
* configured server default base rate is used when request base rate is omitted;
* request base rate overrides configured server default;
* missing request base rate and missing configured default returns business error;
* missing exchange rate returns business error;
* invalid due date returns business error;
* unsupported receivable type returns business error.

Example test names:

```text id="2zjonq"
shouldSimulatePricingWithoutPersistingSettlement
shouldUseLatestExchangeRateForCrossCurrencySimulation
shouldFailSimulationWhenExchangeRateIsMissing
shouldFailSimulationForUnsupportedReceivableType
```

## 7.4 Create Settlement Service Tests

Settlement tests are critical.

Test:

* valid batch creates settlement and items;
* settlement is associated with assignor;
* all items share one source currency;
* configured server default base rate is used when request base rate is omitted;
* settlement totals are calculated from items;
* settlement item stores calculation snapshot;
* receivable status changes to `SETTLED`;
* duplicate receivable settlement fails;
* missing exchange rate fails;
* invalid item fails the entire batch;
* no partial settlement is persisted when failure occurs.

Example test names:

```text id="zpqdr0"
shouldCreateSettlementForValidBatch
shouldPersistSettlementItemCalculationSnapshots
shouldUpdateReceivablesToSettled
shouldRejectEmptySettlementBatch
shouldRejectMixedSourceCurrencyBatch
shouldFailWhenExchangeRateIsMissing
shouldFailWhenReceivableIsAlreadySettled
shouldRollbackEntireSettlementWhenAnyItemFails
```

## 7.5 Statement Service Tests

Test:

* filters by date range;
* filters by assignor;
* filters by payment currency;
* filters by source currency;
* filters by receivable type;
* validates invalid date range;
* applies default pagination;
* enforces maximum page size;
* uses deterministic sorting.

Example test names:

```text id="mgbt0l"
shouldFilterStatementByDateRange
shouldFilterStatementByAssignor
shouldFilterStatementByPaymentCurrency
shouldFilterStatementByReceivableType
shouldRejectInvalidDateRange
shouldApplyDefaultPagination
shouldRejectPageSizeAboveLimit
```

## 8. Infrastructure Integration Tests

## 8.1 Purpose

Infrastructure tests verify real database behavior.

These tests should use MySQL through Testcontainers when possible, because H2 may not match MySQL behavior.

## 8.2 Required Integration Test Areas

### 8.2.1 Flyway Migration Test

Test:

* migrations run from empty database;
* schema validates successfully;
* reference data is seeded;
* Hibernate validation passes if tested with context.

Example test names:

```text id="fpz4i3"
shouldRunFlywayMigrationsSuccessfully
shouldSeedSupportedCurrencies
shouldSeedReceivableTypes
```

### 8.2.2 Exchange Rate Repository Test

Test:

* saves exchange rate;
* retrieves latest exchange rate by exact pair;
* does not return inverse pair;
* orders by `valid_at` descending and deterministic tie-breaker.

Example test names:

```text id="nylc2g"
shouldSaveExchangeRate
shouldFindLatestExchangeRateByExactPair
shouldNotFindInverseExchangeRateWhenOnlyOppositePairExists
shouldReturnMostRecentValidExchangeRate
```

### 8.2.3 Duplicate Settlement Constraint Test

Test:

* same receivable cannot appear in two settlement items;
* database unique constraint prevents duplicate settlement.

Example test names:

```text id="rnjyma"
shouldPreventSameReceivableInMultipleSettlementItems
```

### 8.2.4 Transaction Rollback Test

Test:

* if settlement item persistence fails, settlement header is rolled back;
* if one item in batch is invalid, no items are persisted.

Example test names:

```text id="b24xke"
shouldRollbackSettlementWhenItemPersistenceFails
shouldPersistNothingWhenBatchContainsInvalidItem
```

### 8.2.5 Statement Query Test

Test:

* query filters at database level;
* pagination returns expected subset;
* sorting is deterministic;
* filters by receivable type require correct join;
* count query matches filtered results.

Example test names:

```text id="rgh7l3"
shouldReturnPaginatedStatementRows
shouldFilterStatementRowsByReceivableType
shouldSortStatementRowsBySettledAtDescending
shouldReturnCorrectTotalElementsForFilteredStatement
```

## 8.3 Avoid H2 for Critical Persistence Tests

Do not rely on H2 for:

* MySQL-specific SQL;
* Flyway compatibility;
* check constraints;
* unique constraints;
* transaction behavior;
* date/time behavior;
* analytical query validation.

Use MySQL Testcontainers for these cases.

## 9. API / Controller Tests

## 9.1 Purpose

API tests verify the HTTP contract.

They should validate:

* endpoint paths;
* request validation;
* response structure;
* status codes;
* structured errors;
* content type;
* JSON field names.

## 9.2 Exchange Rate API Tests

Endpoint:

```text id="1k2ggg"
POST /api/exchange-rates
GET  /api/exchange-rates/latest
```

Test:

* create exchange rate returns `201 Created`;
* invalid same source/target returns `400 Bad Request`;
* negative rate returns `400 Bad Request`;
* latest missing rate returns `404 Not Found`;
* response includes expected fields.

Example test names:

```text id="7okyj3"
shouldCreateExchangeRate
shouldRejectExchangeRateWithSameSourceAndTarget
shouldRejectExchangeRateWithNonPositiveRate
shouldReturnNotFoundWhenLatestExchangeRateDoesNotExist
```

## 9.3 Pricing Simulation API Tests

Endpoint:

```text id="mn0wk3"
POST /api/pricing/simulations
```

Test:

* valid same-currency request returns `200 OK`;
* valid cross-currency request returns `200 OK`;
* invalid face value returns `400 Bad Request`;
* past due date returns `422 Unprocessable Entity`;
* missing exchange rate returns `422 Unprocessable Entity`;
* unsupported receivable type returns `422 Unprocessable Entity`;
* response includes pricing details.

Example test names:

```text id="j0jjrn"
shouldSimulateSameCurrencyPricing
shouldSimulateCrossCurrencyPricing
shouldRejectInvalidFaceValue
shouldRejectPastDueDate
shouldReturnUnprocessableEntityWhenExchangeRateIsMissing
shouldReturnUnprocessableEntityForUnsupportedReceivableType
```

## 9.4 Settlement API Tests

Endpoint:

```text id="vj80qe"
POST /api/settlements
GET  /api/settlements/{id}
```

Test:

* valid settlement returns `201 Created`;
* response contains `Location` header;
* empty batch returns `400 Bad Request`;
* duplicate settlement returns `409 Conflict`;
* mixed source currency batch returns `422 Unprocessable Entity`;
* missing exchange rate returns `422 Unprocessable Entity`;
* settlement detail returns persisted audit values;
* not found settlement returns `404 Not Found`.

Example test names:

```text id="b58vja"
shouldCreateSettlementBatch
shouldReturnLocationHeaderWhenSettlementIsCreated
shouldRejectEmptySettlementBatch
shouldRejectDuplicateSettlement
shouldRejectMixedSourceCurrencySettlementBatch
shouldReturnSettlementDetailWithAuditSnapshot
shouldReturnNotFoundForUnknownSettlement
```

## 9.5 Statement API Tests

Endpoint:

```text id="6ruj5m"
GET /api/settlements/statement
```

Test:

* returns paginated statement;
* applies default pagination;
* rejects invalid page size;
* rejects invalid date range;
* filters by period;
* filters by assignor;
* filters by currency;
* filters by receivable type.

Example test names:

```text id="5hcnat"
shouldReturnPaginatedStatement
shouldApplyDefaultStatementPagination
shouldRejectInvalidStatementPageSize
shouldRejectInvalidStatementDateRange
shouldFilterStatementByCurrency
shouldFilterStatementByReceivableType
```

## 9.6 Reference Data API Tests

Endpoint:

```text id="w5v1mv"
GET /api/reference-data/currencies
GET /api/reference-data/receivable-types
```

Test:

* returns BRL and USD;
* returns Mercantile Duplicate and Post-Dated Check;
* spreads are decimal values;
* no frontend hardcoding is required.

Example test names:

```text id="mo4cs1"
shouldListSupportedCurrencies
shouldListReceivableTypesWithMonthlySpreads
```

## 10. Frontend Tests

## 10.1 Purpose

Frontend tests verify that the Angular application correctly interacts with backend contracts and enforces usability-level behavior.

Frontend tests do not validate official financial formulas.

## 10.2 Pricing Simulation Screen Tests

Test:

* form starts invalid;
* face value is required;
* face value must be greater than zero;
* base rate is required;
* due date must be future;
* valid form calls backend simulation endpoint;
* result panel displays backend response;
* backend validation error is displayed.

Example test names:

```text id="x0ktrj"
shouldStartPricingSimulationFormInvalid
shouldValidateRequiredPricingFields
shouldCallSimulationApiWhenFormIsValid
shouldDisplaySimulationResult
shouldDisplayBackendValidationError
```

## 10.3 Exchange Rate Screen Tests

Test:

* source currency required;
* target currency required;
* source and target must differ;
* rate must be positive;
* valid form calls create exchange-rate endpoint;
* success message is displayed.

Example test names:

```text id="m7b0jr"
shouldValidateExchangeRateForm
shouldRejectSameSourceAndTargetCurrencies
shouldCallCreateExchangeRateApi
```

## 10.4 Settlement Statement Grid Tests

Test:

* initial load requests first page;
* changing page requests backend;
* changing filters requests backend;
* grid displays returned rows;
* empty state is displayed;
* error state is displayed;
* pagination is not local-only.

Example test names:

```text id="5s7x3i"
shouldLoadFirstStatementPage
shouldRequestBackendWhenPageChanges
shouldRequestBackendWhenFiltersChange
shouldDisplayStatementRows
shouldDisplayEmptyState
shouldDisplayStatementError
```

## 10.5 Frontend API Service Tests

Test API services for correct URLs and parameters.

Examples:

```text id="ah2olb"
shouldCallPricingSimulationEndpoint
shouldCallCreateExchangeRateEndpoint
shouldCallSettlementStatementEndpointWithFiltersAndPagination
```

## 11. Manual End-to-End Validation

Manual validation should be performed before final delivery.

Manual validation is not a substitute for automated tests.

## 11.1 Local Startup

Validate:

```bash id="95fw61"
docker compose up --build
```

Expected:

* MySQL container healthy;
* backend starts;
* Flyway migrations run;
* frontend starts;
* Swagger accessible;
* frontend accessible.

## 11.2 Exchange Rate Flow

Steps:

1. Open Swagger or frontend.
2. Create exchange rate `BRL -> USD`.
3. Retrieve latest rate.
4. Verify exact direction.
5. Try inverse direction without registering it.
6. Confirm inverse direction fails.

## 11.3 Pricing Simulation Flow

Steps:

1. Simulate same-currency BRL pricing.
2. Verify exchange rate is not required.
3. Simulate BRL receivable paid in USD.
4. Verify exchange rate is used.
5. Verify present value is calculated before conversion.
6. Test invalid due date.
7. Test unsupported receivable type.

## 11.4 Settlement Flow

Steps:

1. Create settlement batch with valid items.
2. Verify response contains settlement ID and items.
3. Retrieve settlement detail.
4. Verify audit snapshot fields.
5. Try settling the same external reference again.
6. Confirm conflict response.
7. Try settlement with invalid item.
8. Confirm no partial persistence.

## 11.5 Statement Flow

Steps:

1. Create multiple settlements.
2. Query statement without filters.
3. Query by date range.
4. Query by payment currency.
5. Query by receivable type.
6. Change page and size.
7. Confirm pagination is server-side.

## 12. Test Data Strategy

## 12.1 Fixed Clock

Tests involving due dates and term calculation should use a fixed clock.

Example fixed date:

```text id="i6xguk"
2026-07-07
```

Rationale:

* term calculation becomes deterministic;
* tests do not fail depending on execution date.

## 12.2 Standard Test Currencies

Use:

```text id="279940"
BRL
USD
```

## 12.3 Standard Test Receivable Types

Use:

```text id="rz8xzl"
MERCANTILE_DUPLICATE
POST_DATED_CHECK
```

## 12.4 Standard Test Rates

Suggested values:

```text id="7p0cml"
baseRate = 0.01000000
mercantileDuplicateSpread = 0.01500000
postDatedCheckSpread = 0.02500000
exchangeRate BRL -> USD = 0.19000000
exchangeRate USD -> BRL = 5.25000000
```

Do not assume inverse rates unless explicitly registered.

## 12.5 Standard Test Assignor

```text id="akubox"
name = ACME Comercio Ltda.
document = 12345678000199
```

## 12.6 Standard Test External References

```text id="zsvxsy"
NF-1001
NF-1002
CHK-2001
CHK-2002
```

## 13. Financial Test Examples

## 13.1 Same-Currency Example

Input:

```text id="2s5ppe"
faceValue = 10000.00
sourceCurrency = BRL
paymentCurrency = BRL
baseRate = 0.01000000
spread = 0.01500000
termInMonths = 2
```

Expected behavior:

```text id="uehnal"
presentValue is calculated in BRL
paymentValue equals presentValue
exchangeRate is null
discountValue = faceValue - presentValue
```

## 13.2 Cross-Currency Example

Input:

```text id="nvb0e9"
faceValue = 10000.00
sourceCurrency = BRL
paymentCurrency = USD
baseRate = 0.01000000
spread = 0.01500000
termInMonths = 2
exchangeRate BRL -> USD = 0.19000000
```

Expected order:

```text id="a6z44v"
1. Calculate presentValue in BRL.
2. Convert presentValue from BRL to USD.
3. Return paymentValue in USD.
4. Persist exchangeRate snapshot if settlement.
```

Test must fail if implementation converts face value first.

## 13.3 Missing Exchange Rate Example

Input:

```text id="o5y5ol"
sourceCurrency = BRL
paymentCurrency = USD
no BRL -> USD exchange rate exists
```

Expected:

```text id="r055p8"
simulation fails with 422
settlement fails with 422
no settlement is persisted
```

## 13.4 Duplicate Settlement Example

Scenario:

```text id="masejz"
receivable externalReference = NF-1001
assignor document = 12345678000199
first settlement succeeds
second settlement with same assignor and externalReference fails
```

Expected:

```text id="gbf58l"
409 Conflict
no duplicate settlement item
```

## 14. Regression Tests for AI-Generated Mistakes

Because AI may be used during development, regression tests should protect against common generated-code defects.

## 14.1 Floating-Point Regression

Review or test to ensure financial calculations do not use:

```text id="31cr1o"
double
float
Double
Float
Math.pow with double-based calculation
```

A static check may be added if time allows.

At minimum, code review must verify this.

## 14.2 Spread Representation Regression

Test that:

```text id="szxvv8"
MERCANTILE_DUPLICATE spread = 0.01500000
POST_DATED_CHECK spread = 0.02500000
```

not:

```text id="y3xlr1"
1.5
2.5
```

## 14.3 Currency Conversion Order Regression

Test that cross-currency conversion occurs after present value calculation.

## 14.4 In-Memory Statement Filtering Regression

Review/test that the statement query does not use:

```text id="y5t0bg"
findAll().stream().filter(...)
```

for production report filtering.

## 14.5 Frontend Formula Duplication Regression

Review/test frontend code to ensure the official pricing formula is not implemented in Angular.

## 15. Coverage Expectations

This project does not require arbitrary high percentage coverage.

Coverage should be meaningful.

Minimum qualitative expectations:

```text id="n69bdo"
- Pricing strategies covered.
- Pricing engine covered.
- Cross-currency behavior covered.
- Validation edge cases covered.
- Settlement atomicity covered.
- Duplicate prevention covered.
- Statement query filters covered.
```

Optional quantitative target:

```text id="lsscoa"
Backend domain/application line coverage: 70%+
```

This target is secondary to business-critical scenario coverage.

## 16. Test Commands

## 16.1 Backend

Expected command:

```bash id="dwbssc"
cd backend
mvn test
```

For full verification:

```bash id="qxef3u"
cd backend
mvn verify
```

If integration tests are separated:

```bash id="acsf8p"
cd backend
mvn verify -P integration-tests
```

The exact Maven profiles may be defined during implementation.

## 16.2 Frontend

Expected commands:

```bash id="bfi8ez"
cd frontend
npm test
```

Build validation:

```bash id="yr2q23"
cd frontend
npm run build
```

Lint validation, if configured:

```bash id="1wwg44"
cd frontend
npm run lint
```

## 16.3 Full Local Validation

Expected command:

```bash id="ce7ks0"
docker compose up --build
```

## 17. CI/CD Testing Expectations

CI/CD is a senior-level differential, but the project should be compatible with a simple GitHub Actions workflow.

Recommended CI checks:

```text id="yg5nol"
backend: mvn test
frontend: npm install && npm run build
```

Optional checks:

```text id="xxe5ha"
backend: mvn verify
frontend: npm test
docker compose config
```

If CI is implemented, document it in the README.

## 18. Test Naming Conventions

Use descriptive test names.

Preferred style:

```text id="n1qtle"
should<ExpectedBehavior>When<Condition>
```

Examples:

```text id="aoe6hu"
shouldApplyExchangeConversionAfterPresentValueCalculation
shouldRejectPastDueDateWhenSimulatingPricing
shouldRollbackSettlementWhenAnyItemIsInvalid
shouldReturnConflictWhenReceivableIsAlreadySettled
```

## 19. Test Organization

## 19.1 Backend Unit Tests

Recommended location:

```text id="dlc038"
backend/src/test/java/com/srm/creditengine/domain
backend/src/test/java/com/srm/creditengine/application
```

## 19.2 Backend Integration Tests

Recommended location:

```text id="49gnmd"
backend/src/test/java/com/srm/creditengine/integration
```

or:

```text id="7x4o5k"
backend/src/integrationTest/java
```

depending on Maven configuration.

## 19.3 Frontend Tests

Recommended location:

```text id="oobigy"
frontend/src/app/**/*.spec.ts
```

## 20. Mocking Strategy

## 20.1 Use Mocks For

* application service dependencies in unit tests;
* repository collaborators when not testing persistence;
* exchange-rate provider if future mock integration exists;
* Angular API responses.

## 20.2 Do Not Mock

Do not mock the pricing engine in tests meant to validate pricing behavior.

Do not mock the database in tests meant to validate:

* Flyway migrations;
* unique constraints;
* rollback behavior;
* SQL statement queries.

## 21. Testcontainers Strategy

Use Testcontainers for MySQL-backed tests when validating:

```text id="lc4fxj"
Flyway migrations
repository behavior
database constraints
transaction rollback
statement query filtering
```

Recommended container:

```text id="i2pttd"
mysql:8.4.10
```

The Testcontainers version should be selected through Maven dependency management where possible.

## 22. Definition of Done for Testing

A feature is not done unless:

```text id="c2pbvh"
- relevant unit tests are added or updated;
- relevant integration tests are added when persistence behavior matters;
- API validation tests are added when endpoint behavior changes;
- frontend tests are added when UI behavior changes materially;
- tests pass locally;
- AI_USAGE.md is updated if AI helped generate or review tests;
- PR description includes test commands executed.
```

## 23. Minimum Required Tests Before Final Delivery

The final delivery must include at least:

```text id="s939px"
1. Pricing strategy unit tests.
2. Pricing engine unit tests.
3. Cross-currency conversion order test.
4. Same-currency pricing test.
5. Missing exchange-rate test.
6. Invalid due-date test.
7. Settlement atomicity or rollback test.
8. Duplicate settlement prevention test.
9. Statement query pagination/filter test.
10. API validation/error response test.
```

Frontend tests are recommended, but backend business-critical tests have higher priority.

## 24. Manual Final Test Checklist

Before final delivery, manually verify:

```text id="z7w8sk"
- docker compose up --build starts all services.
- Swagger is accessible.
- Reference data endpoints return BRL, USD and receivable types.
- Exchange rate can be created.
- Latest exchange rate can be retrieved.
- Pricing simulation works for same-currency case.
- Pricing simulation works for cross-currency case.
- Settlement batch can be created.
- Duplicate settlement is rejected.
- Settlement detail shows audit snapshot.
- Statement endpoint filters and paginates.
- Angular frontend loads.
- Angular simulation screen calls backend.
- Angular settlement grid uses server-side pagination.
```

## 25. Related Documents

```text id="b686i9"
README.md
AGENTS.md
AI_USAGE.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/08-acceptance-criteria.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-005-frontend-stack.md
docs/adr/ADR-007-ai-assisted-development.md
```

## 26. Change Policy

When test strategy changes, update:

```text id="h05uai"
docs/specs/07-testing-strategy.md
docs/specs/08-acceptance-criteria.md
AGENTS.md
docs/prompts/*
AI_USAGE.md if AI influenced the change
```

When implementation adds behavior without tests, document why in the relevant PR.

Business-critical behavior should not be left untested.
