# 05 - Settlement Flow Prompt

## Purpose

Use this prompt to implement the settlement flow for the **SRM Credit Engine**.

This prompt focuses on the most critical transactional feature of the system:

* settlement batch creation;
* assignor resolution or creation;
* receivable resolution or creation;
* receivable validation;
* one source currency per batch validation;
* pricing calculation for each item;
* cross-currency exchange-rate handling;
* settlement header persistence;
* settlement item audit snapshot persistence;
* receivable status update;
* duplicate settlement prevention;
* transaction rollback;
* settlement detail retrieval;
* tests for atomicity and auditability.

This is a business-critical task.

All generated code must be reviewed carefully before commit.

## Target Branch

Recommended branch:

```text id="7jrta7"
feature/settlement-flow
```

Recommended commit messages:

```text id="tkd8re"
feat: implement atomic settlement flow
test: cover settlement rollback and duplicate prevention
```

## Prerequisites

Before running this prompt, the following should already exist:

```text id="ey4d3i"
[ ] Backend scaffold exists.
[ ] Database migrations exist.
[ ] Required tables exist.
[ ] Currency engine exists.
[ ] Pricing engine exists.
[ ] Reference data exists for BRL and USD.
[ ] Reference data exists for receivable types.
[ ] Global exception handling exists.
[ ] API validation structure exists.
```

Required tables:

```text id="a2o8lj"
assignors
receivables
settlements
settlement_items
currencies
receivable_types
exchange_rates
```

If pricing engine or exchange-rate lookup does not exist yet, stop and report the missing prerequisite instead of inventing fake settlement calculations.

## Prompt

```text id="5rb4b7"
You are working on the SRM Credit Engine repository.

This task is to implement only the settlement creation flow and settlement detail retrieval.

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
Implement the settlement flow.

Scope:
You may change:
- backend/src/main/java
- backend/src/test/java
- backend/src/main/resources/db/migration only if a required settlement-related constraint or index is missing
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
- Implement settlement creation endpoint:
  POST /api/settlements
- Implement settlement detail endpoint:
  GET /api/settlements/{id}
- Follow docs/specs/04-api-contract.md.
- Settlement request must receive assignor data.
- Settlement request must receive paymentCurrency.
- Settlement request may receive baseRate.
- Settlement request must receive one or more receivables.
- Maximum settlement batch size is 100.
- Empty settlement batch must be rejected.
- All receivables in the same settlement batch must share the same source currency.
- Mixed-source-currency batches must be rejected.
- Settlement creation must be atomic.
- If any item fails validation or persistence, nothing from the settlement may be persisted.
- Each settlement item must store the calculation snapshot.
- Receivable status must be updated to SETTLED after successful settlement.
- Duplicate settlement must be prevented.
- Settlement detail must return persisted audit values, not recalculated values.

API contract requirements:
Implement:

POST /api/settlements

GET /api/settlements/{id}

Request for POST /api/settlements:
{
  "assignor": {
    "name": "ACME Comercio Ltda.",
    "document": "12345678000199"
  },
  "paymentCurrency": "USD",
  "baseRate": 0.01000000,
  "receivables": [
    {
      "externalReference": "NF-1001",
      "faceValue": 10000.00,
      "sourceCurrency": "BRL",
      "receivableType": "MERCANTILE_DUPLICATE",
      "dueDate": "2026-09-07"
    }
  ]
}

Expected response:
- 201 Created
- Location header
- settlement id
- assignor
- source currency
- payment currency
- status
- base rate
- item count
- total face value
- total present value
- total payment value
- settledAt
- settlement item snapshots

Base-rate resolution requirements:
- If `baseRate` is provided in the request, use it.
- Otherwise resolve the effective rate from server-side `DEFAULT_BASE_RATE`.
- If neither source is available, fail with structured business error and no persistence.

Settlement item snapshot must include:
- item id
- receivable id
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
- exchange rate, when applicable
- calculatedAt

Architecture requirements:
- Controller must be thin.
- Controller must not calculate pricing.
- Controller must not manage transactions.
- CreateSettlementService must own the transaction boundary.
- Application service must orchestrate the settlement use case.
- PricingEngine must be reused for item pricing.
- Exchange-rate lookup must use the existing currency engine or repository abstraction.
- Repositories must handle persistence.
- Domain/application code must enforce business rules.
- Settlement detail must read persisted values.
- Do not recalculate settlement detail dynamically from current rates or current spreads.

Expected components:
- SettlementController
- CreateSettlementRequest
- CreateSettlementResponse or SettlementResponse
- SettlementItemResponse
- AssignorRequest / AssignorResponse
- SettlementResponseMapper or equivalent
- CreateSettlementService
- GetSettlementService
- SettlementRepository
- ReceivableRepository
- AssignorRepository
- SettlementEntity
- SettlementItemEntity
- ReceivableEntity
- AssignorEntity
- Settlement
- SettlementItem
- Receivable
- Assignor
- SettlementStatus
- ReceivableStatus
- DuplicateSettlementException or equivalent
- SettlementNotFoundException or equivalent
- InvalidSettlementBatchException or equivalent
- MixedSourceCurrencyException or equivalent

Persistence requirements:
Use the schema defined in docs/specs/05-data-model.md.

Required tables:
- assignors
- receivables
- settlements
- settlement_items

Required settlement item uniqueness:
- settlement_items.receivable_id must be unique.

Required receivable uniqueness:
- receivables must be unique by assignor_id and external_reference.

Required settlement behavior:
- Create or resolve assignor.
- Create or resolve receivables by assignor and externalReference.
- Reject receivable if already SETTLED.
- Reject receivable if CANCELLED.
- Only AVAILABLE receivables may be settled.
- Persist settlement header.
- Persist settlement items.
- Update receivable statuses to SETTLED.
- Commit only if the full batch succeeds.

Transaction requirements:
- Use @Transactional on CreateSettlementService or equivalent application service.
- Do not place @Transactional on the controller as the main boundary.
- Ensure failure in any item rolls back the whole settlement.
- Ensure database uniqueness constraint remains the final protection against duplicate settlement.
- Map duplicate constraint violations to structured 409 Conflict when possible.

Financial requirements:
- Use BigDecimal for all financial values.
- Do not use double, float, Double or Float for financial values.
- Do not construct BigDecimal from floating-point literals.
- Use the existing PricingEngine for calculation.
- Do not duplicate pricing formula in settlement service if PricingEngine already exists.
- Cross-currency conversion must happen after present value calculation.
- Same-currency settlement must not require exchange rate.
- Missing exchange rate for cross-currency settlement must fail with 422.
- Settlement item must persist exchange-rate snapshot when cross-currency.
- Settlement item exchangeRate may be null when same-currency.
- Historical values must not be recomputed from current exchange rates.

Settlement totals:
- totalFaceValue is the sum of item face values in source currency.
- totalPresentValue is the sum of item present values in source currency.
- totalPaymentValue is the sum of item payment values in payment currency.
- itemCount is the number of settlement items.
- All totals must use BigDecimal.
- All items in the batch must share the same source currency.

Validation requirements:
- assignor is required.
- assignor.name is required and not blank.
- assignor.document is optional.
- paymentCurrency is required and supported.
- baseRate is optional, but when provided it must be greater than or equal to zero.
- if baseRate is omitted, backend must resolve effective base rate from `DEFAULT_BASE_RATE` or fail clearly.
- receivables list is required.
- receivables list must contain 1 to 100 items.
- externalReference is required and not blank.
- faceValue is required and greater than zero.
- sourceCurrency is required and supported.
- receivableType is required and supported.
- dueDate is required and future.
- mixed source currency batch must fail.
- duplicate receivable in the same request must fail.
- duplicate already-settled receivable must fail.

Expected error behavior:
- Empty batch: 400 Bad Request.
- Batch above 100 items: 400 Bad Request.
- Invalid fields: 400 Bad Request.
- Past or same-day due date: 422 Unprocessable Entity.
- Missing exchange rate: 422 Unprocessable Entity.
- Mixed source currency: 422 Unprocessable Entity.
- Duplicate settlement: 409 Conflict.
- Unknown settlement id: 404 Not Found.
- Unexpected errors: structured 500 response without stack trace.

Testing requirements:
Add or update tests for:
- create valid same-currency settlement.
- create valid cross-currency settlement.
- persist settlement header.
- persist settlement item snapshots.
- update receivable status to SETTLED.
- calculate settlement totals from items.
- reject empty batch.
- reject batch with more than 100 items.
- reject mixed-source-currency batch.
- reject duplicate receivable inside the same request.
- reject already-settled receivable.
- rollback entire settlement when any item fails.
- missing exchange rate causes no settlement persistence.
- duplicate settlement returns 409.
- settlement detail returns persisted values.
- settlement detail does not recalculate values after new exchange rate is registered.
- unknown settlement id returns 404.
- structured error format is preserved.
- no floating-point financial code is introduced.

Expected commands:
- cd backend
- mvn test

If integration tests are separated:
- cd backend
- mvn verify

Constraints:
- Do not implement statement query in this task.
- Do not implement frontend in this task.
- Do not add a real banking payment integration.
- Do not add authentication or authorization.
- Do not add Kafka, queues or asynchronous processing.
- Do not introduce unnecessary dependencies.
- Do not change API contracts without reporting the mismatch first.
- Do not silently remove existing tests.
- Do not weaken existing validation.

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
- Confirm transaction boundary.
- Confirm settlement atomicity.
- Confirm duplicate settlement prevention.
- Confirm audit snapshot persistence.
- Confirm one source currency per batch.
- Confirm BigDecimal usage.
- Confirm no double/float financial code.
- Confirm settlement detail reads persisted values.
- Confirm controller remains thin.

Risks or follow-ups:
- ...
```

## Expected AI Output

The AI agent should implement a focused settlement feature.

Expected backend additions may include files similar to:

```text id="64znuk"
backend/src/main/java/com/srm/creditengine/api/controller/SettlementController.java
backend/src/main/java/com/srm/creditengine/api/request/CreateSettlementRequest.java
backend/src/main/java/com/srm/creditengine/api/request/AssignorRequest.java
backend/src/main/java/com/srm/creditengine/api/request/ReceivableSettlementRequest.java
backend/src/main/java/com/srm/creditengine/api/response/SettlementResponse.java
backend/src/main/java/com/srm/creditengine/api/response/SettlementItemResponse.java
backend/src/main/java/com/srm/creditengine/api/response/AssignorResponse.java
backend/src/main/java/com/srm/creditengine/application/settlement/CreateSettlementService.java
backend/src/main/java/com/srm/creditengine/application/settlement/GetSettlementService.java
backend/src/main/java/com/srm/creditengine/domain/settlement/Settlement.java
backend/src/main/java/com/srm/creditengine/domain/settlement/SettlementItem.java
backend/src/main/java/com/srm/creditengine/domain/settlement/SettlementStatus.java
backend/src/main/java/com/srm/creditengine/domain/receivable/Receivable.java
backend/src/main/java/com/srm/creditengine/domain/receivable/ReceivableStatus.java
backend/src/main/java/com/srm/creditengine/domain/receivable/ReceivableType.java
backend/src/main/java/com/srm/creditengine/infrastructure/persistence/SettlementEntity.java
backend/src/main/java/com/srm/creditengine/infrastructure/persistence/SettlementItemEntity.java
backend/src/main/java/com/srm/creditengine/infrastructure/persistence/ReceivableEntity.java
backend/src/main/java/com/srm/creditengine/infrastructure/persistence/AssignorEntity.java
backend/src/main/java/com/srm/creditengine/infrastructure/repository/SettlementJpaRepository.java
backend/src/main/java/com/srm/creditengine/infrastructure/repository/SettlementItemJpaRepository.java
backend/src/main/java/com/srm/creditengine/infrastructure/repository/ReceivableJpaRepository.java
backend/src/main/java/com/srm/creditengine/infrastructure/repository/AssignorJpaRepository.java
```

Exact names may vary if they remain aligned with the architecture and API contract.

## Required API Contract

## 1. Create Settlement Batch

```http id="xt6m4m"
POST /api/settlements
```

Request:

```json id="sw4rmi"
{
  "assignor": {
    "name": "ACME Comercio Ltda.",
    "document": "12345678000199"
  },
  "paymentCurrency": "USD",
  "baseRate": 0.01000000,
  "receivables": [
    {
      "externalReference": "NF-1001",
      "faceValue": 10000.00,
      "sourceCurrency": "BRL",
      "receivableType": "MERCANTILE_DUPLICATE",
      "dueDate": "2026-09-07"
    }
  ]
}
```

Expected status:

```http id="e2s10o"
201 Created
```

Expected header:

```http id="0mrsfg"
Location: /api/settlements/{id}
```

Response must include:

```text id="8mp6kl"
id
assignor
sourceCurrency
paymentCurrency
status
baseRate
itemCount
totalFaceValue
totalPresentValue
totalPaymentValue
settledAt
items
```

## 2. Get Settlement by ID

```http id="0z96ak"
GET /api/settlements/{id}
```

Expected status when found:

```http id="1bqcpb"
200 OK
```

Expected status when not found:

```http id="58u3yq"
404 Not Found
```

The response must use persisted settlement and settlement item values.

## Business Rules to Preserve

## 1. Atomic Settlement

A settlement batch is all-or-nothing.

Correct:

```text id="9jc68y"
All items valid -> settlement and all items are persisted.
Any item invalid -> nothing is persisted.
```

Incorrect:

```text id="iutvkd"
Settlement header persisted but one or more items missing.
Some receivables updated to SETTLED while others fail.
```

## 2. Duplicate Settlement Prevention

The same receivable must not be settled twice.

Required protections:

```text id="rb5ddo"
1. Application-level validation.
2. settlement_items.receivable_id unique constraint.
3. Optional optimistic locking through receivables.version.
```

## 3. Audit Snapshot

Settlement item must store the values used at settlement time.

Required fields:

```text id="rzuqfo"
externalReference
receivableType
faceValue
sourceCurrency
paymentCurrency
baseRate
spread
termInMonths
presentValueInSourceCurrency
discountValue
paymentValue
exchangeRate
calculatedAt
```

Historical settlement detail must not call PricingEngine again to recalculate old values.

## 4. One Source Currency per Batch

The initial implementation must reject mixed source currency batches.

Valid:

```text id="uim6ns"
Item 1 sourceCurrency = BRL
Item 2 sourceCurrency = BRL
paymentCurrency = USD
```

Invalid:

```text id="zffpzc"
Item 1 sourceCurrency = BRL
Item 2 sourceCurrency = USD
paymentCurrency = USD
```

## 5. Cross-Currency Settlement

Correct order:

```text id="09db3o"
1. Calculate present value in source currency.
2. Convert present value to payment currency.
3. Persist payment value in payment currency.
4. Persist exchange-rate snapshot.
```

## Required Tests

## 1. Settlement Creation Tests

Required cases:

```text id="5bm2bq"
shouldCreateSameCurrencySettlement
shouldCreateCrossCurrencySettlement
shouldPersistSettlementHeader
shouldPersistSettlementItemSnapshots
shouldUpdateReceivableStatusToSettled
shouldCalculateSettlementTotalsFromItems
```

## 2. Settlement Validation Tests

Required cases:

```text id="2nj4f7"
shouldRejectEmptySettlementBatch
shouldRejectSettlementBatchAboveMaximumSize
shouldRejectMixedSourceCurrencyBatch
shouldRejectDuplicateReceivableInsideSameRequest
shouldRejectPastDueDateReceivable
shouldRejectUnsupportedReceivableType
```

## 3. Atomicity and Duplicate Tests

Required cases:

```text id="0fuhke"
shouldRollbackEntireSettlementWhenAnyItemFails
shouldPersistNothingWhenExchangeRateIsMissing
shouldRejectAlreadySettledReceivable
shouldReturnConflictForDuplicateSettlement
shouldPreventDuplicateSettlementAtDatabaseConstraintLevel
```

## 4. Settlement Detail Tests

Required cases:

```text id="9p90ql"
shouldReturnSettlementDetailWithPersistedSnapshots
shouldReturnNotFoundForUnknownSettlement
shouldNotRecalculateHistoricalSettlementAfterNewExchangeRateIsRegistered
```

## 5. API Error Tests

Required cases:

```text id="h1m9ds"
shouldReturnStructuredErrorForEmptyBatch
shouldReturnStructuredErrorForMixedSourceCurrencyBatch
shouldReturnStructuredErrorForMissingExchangeRate
shouldReturnStructuredErrorForDuplicateSettlement
shouldReturnStructuredErrorForUnknownSettlement
```

## Acceptance Criteria

This task is acceptable when:

```text id="mwf9j8"
[ ] POST /api/settlements exists.
[ ] GET /api/settlements/{id} exists.
[ ] Request DTO matches API contract.
[ ] Response DTO matches API contract.
[ ] Settlement creation is transactional.
[ ] Empty batch is rejected.
[ ] Batch above 100 items is rejected.
[ ] Mixed source currency batch is rejected.
[ ] Duplicate settlement is prevented.
[ ] Receivable status is updated to SETTLED.
[ ] Settlement header is persisted.
[ ] Settlement item snapshots are persisted.
[ ] Settlement totals are calculated from items.
[ ] Cross-currency settlement stores exchange-rate snapshot.
[ ] Same-currency settlement does not require exchange rate.
[ ] Missing exchange rate fails with 422.
[ ] Duplicate settlement fails with 409.
[ ] Unknown settlement detail fails with 404.
[ ] Failed settlement leaves no partial records.
[ ] Settlement detail reads persisted values.
[ ] Financial values use BigDecimal.
[ ] No financial calculation uses double or float.
[ ] Controllers remain thin.
[ ] Tests cover atomicity and duplicate prevention.
[ ] mvn test or mvn verify passes.
```

## Review Checklist for Author

After Codex completes the task, verify:

```text id="2s1v88"
[ ] Did it avoid changing unrelated files?
[ ] Did it implement only settlement creation/detail?
[ ] Did it avoid statement query implementation?
[ ] Did it avoid frontend changes?
[ ] Did it keep controllers thin?
[ ] Is @Transactional placed in application service?
[ ] Does failure in any item rollback the whole batch?
[ ] Are settlement item snapshots persisted?
[ ] Does settlement detail read persisted values?
[ ] Is duplicate settlement prevented in application logic?
[ ] Is duplicate settlement prevented by database constraint?
[ ] Are receivable statuses updated consistently?
[ ] Is one source currency per batch enforced?
[ ] Is BigDecimal used for all financial values?
[ ] Are double/float absent from financial code?
[ ] Is PricingEngine reused rather than duplicated?
[ ] Is exchange-rate snapshot stored?
[ ] Are structured errors returned?
[ ] Did it add meaningful tests?
[ ] Did mvn test or mvn verify pass?
[ ] Does AI_USAGE.md need an update?
```

## Common AI Mistakes to Reject

Reject or correct the output if the AI:

```text id="u1ba2i"
- places transaction boundary in controller;
- persists settlement header before validation and leaves partial records;
- does not rollback when one item fails;
- recalculates settlement detail from current rates;
- fails to store exchange-rate snapshot;
- allows duplicate settlement;
- relies only on application validation without database uniqueness;
- allows mixed source currency batches without grouped totals;
- uses double or float for financial values;
- duplicates pricing formula instead of reusing PricingEngine;
- converts face value before present value calculation;
- silently inverts exchange rates;
- implements statement query in this task;
- changes frontend files;
- adds asynchronous processing unnecessarily;
- adds payment integration;
- weakens existing tests or validation.
```

## Suggested PR Description

````md id="6w2at8"
## Summary

Implements atomic settlement creation and settlement detail retrieval.

## Specs Covered

- docs/specs/03-business-rules.md
- docs/specs/04-api-contract.md
- docs/specs/05-data-model.md
- docs/specs/06-architecture.md
- docs/specs/07-testing-strategy.md
- docs/specs/08-acceptance-criteria.md
- docs/adr/ADR-002-database-choice.md
- docs/adr/ADR-003-money-precision.md
- docs/adr/ADR-004-architecture-style.md

## Changes

- Added settlement creation endpoint.
- Added settlement detail endpoint.
- Added transactional settlement application service.
- Added assignor and receivable persistence flow.
- Added settlement item audit snapshots.
- Added duplicate settlement prevention.
- Added receivable status update.
- Added tests for settlement atomicity and duplicate prevention.

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

* Settlement batches are restricted to one source currency in the initial version.
* Failed settlement attempts are not persisted as audit events in the initial version.
* Authentication and authorization are outside the initial scope.

## AI Usage

AI was used for:

* implementation support
* test generation support

AI_USAGE.md:

* [ ] Updated
* [ ] Not applicable

````id="os9vjj"

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
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-007-ai-assisted-development.md
````

