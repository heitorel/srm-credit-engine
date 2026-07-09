# 07 - Statement Query Prompt

## Purpose

Use this prompt to implement the settlement statement query feature for the **SRM Credit Engine**.

This prompt focuses on the analytical/read side of the application:

* settlement statement endpoint;
* settlement-level historical summary;
* filters by period, assignor, currency, receivable type and status;
* server-side pagination;
* database-level filtering;
* deterministic sorting;
* optimized read query or projection;
* structured validation errors;
* tests for filtering, pagination and query behavior.

This task must not implement pricing, settlement creation or frontend behavior.

## Target Branch

Recommended branch:

```text
feature/statement-query
```

Recommended commit messages:

```text
feat: add settlement statement query
test: cover statement query filters
```

## Prerequisites

Before running this prompt, the following should already exist:

```text
[ ] Backend scaffold exists.
[ ] Database migrations exist.
[ ] Settlement tables exist.
[ ] Settlement creation flow exists or representative test data can be inserted.
[ ] Settlement item snapshots exist.
[ ] Global exception handling exists.
[ ] API validation structure exists.
```

Required tables:

```text
assignors
settlements
settlement_items
receivables
currencies
receivable_types
```

If settlement persistence does not exist yet, stop and report that the settlement flow should be implemented first, unless the task explicitly allows creating isolated query tests with seeded data.

## Prompt

```text
You are working on the SRM Credit Engine repository.

This task is to implement only the settlement statement query feature.

Read first:
- AGENTS.md
- README.md
- AI_USAGE.md
- docs/specs/02-domain-glossary.md
- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/05-data-model.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-001-backend-stack.md
- docs/adr/ADR-002-database-choice.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-004-architecture-style.md
- docs/adr/ADR-007-ai-assisted-development.md

Task:
Implement the settlement statement query endpoint.

Scope:
You may change:
- backend/src/main/java
- backend/src/test/java
- backend/src/main/resources/db/migration only if a required statement-query index is missing
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

Functional requirements:
- Implement settlement statement endpoint:
  GET /api/settlements/statement
- Follow docs/specs/04-api-contract.md.
- Return paginated settlement-level statement rows.
- Support filtering by period.
- Support filtering by assignorId.
- Support filtering by assignorDocument.
- Support filtering by paymentCurrency.
- Support filtering by sourceCurrency.
- Support filtering by receivableType.
- Support filtering by status.
- Support default pagination.
- Support maximum page size validation.
- Support deterministic sorting.
- Default sort must be settledAt descending.
- Query must use persisted settlement values.
- Query must not recalculate historical settlement values.
- Query must not load all rows into memory before filtering.
- Query must not paginate in memory after loading all records.

API contract requirements:
Implement:

GET /api/settlements/statement

Expected query parameters:
- from
- to
- assignorId
- assignorDocument
- paymentCurrency
- sourceCurrency
- receivableType
- status
- page
- size
- sort

Default pagination:
- page = 0
- size = 20

Maximum page size:
- 100

Default sort:
- settledAt,desc

Expected response:
{
  "content": [
    {
      "settlementId": "7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df",
      "assignorId": "5b66f1d1-906e-42cc-bdd2-f7d9d8a08389",
      "assignorName": "ACME Comércio Ltda.",
      "assignorDocument": "12345678000199",
      "sourceCurrency": "BRL",
      "paymentCurrency": "USD",
      "status": "SETTLED",
      "itemCount": 2,
      "totalFaceValue": 15000.00,
      "totalPresentValue": 13980.15,
      "totalPaymentValue": 2662.89,
      "settledAt": "2026-07-07T13:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}

Architecture requirements:
- Controller must be thin.
- Controller must validate and pass query parameters to the application/query layer.
- Application service must orchestrate the statement query.
- Infrastructure query repository must handle database-level filtering and pagination.
- Use projections, criteria queries, query builders or native SQL where appropriate.
- Do not place SQL construction or filtering logic in the controller.
- Do not place financial calculation logic in the statement query.
- Do not call PricingEngine to display statement values.
- Read persisted values from settlements and settlement_items.

Expected components:
- SettlementStatementController
- SettlementStatementRequest or SettlementStatementFilter
- SettlementStatementResponse
- SettlementStatementRowResponse
- PageResponse or equivalent generic page response
- SettlementStatementService
- SettlementStatementQueryRepository
- SettlementStatementRow projection or DTO
- InvalidStatementDateRangeException or equivalent
- InvalidPaginationException or equivalent

Data model requirements:
Use the schema defined in docs/specs/05-data-model.md.

Primary tables:
- settlements
- assignors
- settlement_items

Expected joins:
- settlements -> assignors
- settlements -> settlement_items when filtering by receivableType or item-level source/payment currency is needed

Expected statement row fields:
- settlementId
- assignorId
- assignorName
- assignorDocument
- sourceCurrency
- paymentCurrency
- status
- itemCount
- totalFaceValue
- totalPresentValue
- totalPaymentValue
- settledAt

Filtering requirements:
- from filters settledAt greater than or equal to start date/time.
- to filters settledAt less than or equal to end date/time, or handles date-only end inclusively.
- assignorId filters settlements.assignor_id.
- assignorDocument filters assignors.document.
- paymentCurrency filters settlements.payment_currency_code.
- sourceCurrency filters settlements.source_currency_code.
- status filters settlements.status.
- receivableType filters settlement_items.receivable_type_code.
- If both from and to are provided, from must be less than or equal to to.
- Unsupported currency values must fail validation.
- Unsupported receivable type values must fail validation.
- Unsupported status values must fail validation.

Pagination requirements:
- page is zero-based.
- page must be greater than or equal to zero.
- size must be greater than zero.
- size must be less than or equal to 100.
- Invalid pagination parameters must return structured 400 Bad Request.
- Pagination must be applied in the database.
- Do not fetch all records and then slice in Java.

Sorting requirements:
- Default sort is settledAt descending.
- Sorting must be deterministic.
- Use settlement id as a tie-breaker where practical.
- Only allow documented or safe sort fields.
- Reject or sanitize unsupported sort fields.
- Do not concatenate unsafe user input into raw SQL.

Performance requirements:
- Filtering must happen at database level.
- Pagination must happen at database level.
- Use indexes defined in docs/specs/05-data-model.md.
- Use a projection instead of loading full aggregate graphs when practical.
- Avoid N+1 query patterns.
- Avoid findAll().stream().filter(...) for production statement filtering.

Financial requirements:
- Statement values must use BigDecimal in Java.
- Statement values must come from persisted totals.
- Do not recalculate totalFaceValue, totalPresentValue or totalPaymentValue from current rules.
- Do not recalculate historical item values from current exchange rates.
- Do not use double, float, Double or Float for statement monetary values.

Expected error behavior:
- Invalid date range: 400 Bad Request.
- Invalid page: 400 Bad Request.
- Invalid size: 400 Bad Request.
- Unsupported currency: 422 Unprocessable Entity or documented validation status.
- Unsupported receivable type: 422 Unprocessable Entity or documented validation status.
- Unsupported status: 422 Unprocessable Entity or documented validation status.
- Unexpected errors: structured 500 response without stack trace.

Testing requirements:
Add or update tests for:
- return first page with default pagination.
- return requested page and size.
- reject negative page.
- reject zero size.
- reject size above 100.
- sort by settledAt descending by default.
- deterministic ordering when settledAt is equal.
- filter by date range.
- reject invalid date range.
- filter by assignorId.
- filter by assignorDocument.
- filter by paymentCurrency.
- filter by sourceCurrency.
- filter by receivableType.
- filter by status.
- combine multiple filters.
- return empty page when no rows match.
- totalElements reflects filtered count.
- query uses persisted settlement values.
- no in-memory filtering approach is used.
- structured error format is preserved.

Expected commands:
- cd backend
- mvn test

If integration tests are separated:
- cd backend
- mvn verify

Constraints:
- Do not implement pricing engine in this task.
- Do not implement settlement creation in this task.
- Do not implement frontend in this task.
- Do not add caching unless explicitly justified.
- Do not add Elasticsearch, data warehouse or external analytics infrastructure.
- Do not introduce unnecessary dependencies.
- Do not use unsafe raw SQL string concatenation.
- Do not change API contracts without reporting the mismatch first.
- Do not weaken existing tests or validation.

Expected output:
Return a concise report in this format:

Summary:
- ...

Files changed:
- ...

Tests:
- Command run:
- Result:

Validation:
- Confirm database-level filtering.
- Confirm server-side pagination.
- Confirm deterministic sorting.
- Confirm statement values are persisted values.
- Confirm no historical recalculation.
- Confirm no double/float financial code.
- Confirm controller remains thin.
- Confirm no pricing/settlement/frontend behavior was implemented.

Risks or follow-ups:
- ...
```

## Expected AI Output

The AI agent should implement a focused statement query feature.

Expected backend additions may include files similar to:

```text
backend/src/main/java/com/srm/creditengine/api/controller/SettlementStatementController.java
backend/src/main/java/com/srm/creditengine/api/request/SettlementStatementRequest.java
backend/src/main/java/com/srm/creditengine/api/response/SettlementStatementResponse.java
backend/src/main/java/com/srm/creditengine/api/response/SettlementStatementRowResponse.java
backend/src/main/java/com/srm/creditengine/api/response/PageResponse.java
backend/src/main/java/com/srm/creditengine/application/statement/SettlementStatementService.java
backend/src/main/java/com/srm/creditengine/application/statement/SettlementStatementFilter.java
backend/src/main/java/com/srm/creditengine/infrastructure/query/SettlementStatementQueryRepository.java
backend/src/main/java/com/srm/creditengine/infrastructure/query/SettlementStatementRowProjection.java
```

Exact names may vary if they remain aligned with the architecture and API contract.

## Required API Contract

## 1. Get Settlement Statement

```http
GET /api/settlements/statement
```

Example request:

```http
GET /api/settlements/statement?from=2026-07-01&to=2026-07-31&paymentCurrency=USD&receivableType=MERCANTILE_DUPLICATE&page=0&size=20
```

Expected response:

```json
{
  "content": [
    {
      "settlementId": "7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df",
      "assignorId": "5b66f1d1-906e-42cc-bdd2-f7d9d8a08389",
      "assignorName": "ACME Comércio Ltda.",
      "assignorDocument": "12345678000199",
      "sourceCurrency": "BRL",
      "paymentCurrency": "USD",
      "status": "SETTLED",
      "itemCount": 2,
      "totalFaceValue": 15000.00,
      "totalPresentValue": 13980.15,
      "totalPaymentValue": 2662.89,
      "settledAt": "2026-07-07T13:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

## Business Rules to Preserve

## 1. Database-Level Filtering

Correct:

```text
Query applies WHERE clauses in SQL/JPA criteria/native query.
```

Incorrect:

```text
Load all settlements into memory and filter with Java streams.
```

Forbidden production pattern:

```java
repository.findAll().stream().filter(...)
```

## 2. Server-Side Pagination

Correct:

```text
Database query uses LIMIT/OFFSET or Spring Pageable at query level.
```

Incorrect:

```text
Fetch all rows and return only a sublist in Java.
```

## 3. Persisted Values Only

Correct:

```text
Statement rows use persisted settlement totals and timestamps.
```

Incorrect:

```text
Statement query calls PricingEngine or recalculates old item values.
```

## 4. Deterministic Sorting

Default:

```text
settledAt descending
```

Tie-breaker:

```text
settlement id
```

## 5. Safe Dynamic Querying

If native SQL is used, do not concatenate raw user input.

Correct:

```text
Use bind parameters.
Validate allowed sort fields.
```

Incorrect:

```java
String sql = "SELECT ... ORDER BY " + request.getSort();
```

## Required Tests

## 1. Pagination Tests

Required cases:

```text
shouldReturnDefaultFirstPage
shouldReturnRequestedPageAndSize
shouldRejectNegativePage
shouldRejectZeroSize
shouldRejectPageSizeAboveMaximum
shouldReturnCorrectTotalElementsForFilteredQuery
```

## 2. Sorting Tests

Required cases:

```text
shouldSortBySettledAtDescendingByDefault
shouldUseDeterministicTieBreakerWhenSettledAtIsEqual
shouldRejectUnsupportedSortField
```

## 3. Filter Tests

Required cases:

```text
shouldFilterByDateRange
shouldRejectInvalidDateRange
shouldFilterByAssignorId
shouldFilterByAssignorDocument
shouldFilterByPaymentCurrency
shouldFilterBySourceCurrency
shouldFilterByReceivableType
shouldFilterByStatus
shouldCombineMultipleFilters
shouldReturnEmptyPageWhenNoRowsMatch
```

## 4. Historical Value Tests

Required cases:

```text
shouldUsePersistedSettlementTotals
shouldNotRecalculateHistoricalValuesFromCurrentExchangeRate
shouldNotCallPricingEngineForStatementRows
```

## 5. Error Response Tests

Required cases:

```text
shouldReturnStructuredErrorForInvalidDateRange
shouldReturnStructuredErrorForInvalidPagination
shouldReturnStructuredErrorForUnsupportedCurrency
shouldReturnStructuredErrorForUnsupportedReceivableType
shouldReturnStructuredErrorForUnsupportedStatus
```

## Acceptance Criteria

This task is acceptable when:

```text
[ ] GET /api/settlements/statement exists.
[ ] Response DTO matches API contract.
[ ] Query supports period filter.
[ ] Query supports assignorId filter.
[ ] Query supports assignorDocument filter.
[ ] Query supports paymentCurrency filter.
[ ] Query supports sourceCurrency filter.
[ ] Query supports receivableType filter.
[ ] Query supports status filter.
[ ] Query supports default pagination.
[ ] Query enforces max page size of 100.
[ ] Invalid date range is rejected.
[ ] Invalid pagination is rejected.
[ ] Default sort is settledAt descending.
[ ] Sorting is deterministic.
[ ] Filtering happens at database level.
[ ] Pagination happens at database level.
[ ] Statement rows use persisted settlement values.
[ ] No historical recalculation is performed.
[ ] No financial calculation uses double or float.
[ ] Controller remains thin.
[ ] Tests cover filters and pagination.
[ ] mvn test or mvn verify passes.
[ ] No pricing, settlement creation or frontend behavior was implemented.
```

## Review Checklist for Author

After Codex completes the task, verify:

```text
[ ] Did it avoid changing unrelated files?
[ ] Did it implement only statement query behavior?
[ ] Did it avoid pricing implementation?
[ ] Did it avoid settlement creation changes except when strictly necessary?
[ ] Did it avoid frontend changes?
[ ] Did it keep the controller thin?
[ ] Does filtering happen in SQL/JPA criteria/query repository?
[ ] Is pagination performed by the database?
[ ] Is sorting deterministic?
[ ] Are unsupported sort fields rejected or safely handled?
[ ] Are query parameters validated?
[ ] Are structured errors returned?
[ ] Are persisted totals used?
[ ] Is PricingEngine not used for statement rows?
[ ] Is there no findAll().stream().filter(...) report implementation?
[ ] Are BigDecimal values preserved?
[ ] Are double/float absent from statement financial code?
[ ] Did it add meaningful tests?
[ ] Did mvn test or mvn verify pass?
[ ] Does AI_USAGE.md need an update?
```

## Common AI Mistakes to Reject

Reject or correct the output if the AI:

```text
- uses repository.findAll().stream().filter(...) for statement queries;
- paginates with subList after loading all rows;
- recalculates settlement values with PricingEngine;
- recalculates values from current exchange rates;
- returns item-level rows instead of settlement-level statement rows unless explicitly requested;
- forgets totalElements or totalPages;
- ignores invalid date ranges;
- accepts page size above 100;
- allows unsafe raw SQL sort concatenation;
- returns nondeterministic ordering;
- uses double or float for monetary values;
- implements pricing changes in this task;
- implements settlement creation changes unrelated to querying;
- changes frontend files;
- adds Elasticsearch or analytics infrastructure unnecessarily.
```

## Suggested PR Description

````md
## Summary

Implements the settlement statement query endpoint with filters and server-side pagination.

## Specs Covered

- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/05-data-model.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-002-database-choice.md
- docs/adr/ADR-004-architecture-style.md

## Changes

- Added settlement statement endpoint.
- Added statement filter model.
- Added paginated statement response.
- Added database-level query for filters.
- Added deterministic sorting.
- Added validation for date range and pagination.
- Added tests for filters and pagination.

## Tests

- [x] Unit tests added/updated
- [x] Integration/API tests added/updated
- [x] Manual validation performed

Commands executed:

```bash
cd backend
mvn test
````

or:

```bash
cd backend
mvn verify
```

## Risks and Trade-offs

* Statement query returns settlement-level rows, not item-level detail.
* Item-level details are available through settlement detail endpoint.
* Query performance depends on indexes defined in Flyway migrations.

## AI Usage

AI was used for:

* implementation support
* test generation support

AI_USAGE.md:

* [ ] Updated
* [ ] Not applicable

````

## Related Documents

```text
AGENTS.md
AI_USAGE.md
docs/specs/02-domain-glossary.md
docs/specs/03-business-rules.md
docs/specs/04-api-contract.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/specs/08-acceptance-criteria.md
docs/adr/ADR-002-database-choice.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-007-ai-assisted-development.md
````

