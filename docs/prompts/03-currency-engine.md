# 03 — Currency Engine Prompt

## Purpose

Use this prompt to implement the currency and exchange-rate engine for the **SRM Credit Engine**.

This prompt focuses on:

* supported currency reference data;
* exchange-rate registration;
* latest exchange-rate lookup;
* explicit exchange-rate direction;
* validation of currency pairs;
* validation of positive exchange rates;
* persistence of exchange-rate history;
* API endpoints for currency operations;
* tests for exchange-rate behavior.

This task must not implement pricing, settlement or frontend behavior.

## Target Branch

Recommended branch:

```text id="pbg7i8"
feature/currency-engine
```

Recommended commit messages:

```text id="ifbl3i"
feat: add exchange rate management
test: cover exchange rate validation
```

## Prerequisites

Before running this prompt, the following should already exist:

```text id="ibkfxb"
[ ] Backend scaffold exists.
[ ] Maven project is configured.
[ ] Base package structure exists.
[ ] Global exception handling exists.
[ ] Database migrations exist or are being implemented in the same feature only if necessary.
[ ] currencies table exists or is defined by migration.
[ ] exchange_rates table exists or is defined by migration.
[ ] Reference currency data exists for BRL and USD.
```

If database migrations are not implemented yet, stop and report that the database migration feature should be created first, unless the task explicitly allows adding the required migrations.

## Prompt

```text id="slweps"
You are working on the SRM Credit Engine repository.

This task is to implement only the currency and exchange-rate engine.

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
Implement the currency and exchange-rate engine.

Scope:
You may change:
- backend/src/main/java
- backend/src/test/java
- backend/src/main/resources/db/migration only if required exchange-rate or currency schema is missing
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
- Implement supported currency reference-data endpoint:
  GET /api/reference-data/currencies
- Implement receivable type reference-data endpoint only if not already implemented:
  GET /api/reference-data/receivable-types
- Implement exchange-rate creation endpoint:
  POST /api/exchange-rates
- Implement latest exchange-rate lookup endpoint:
  GET /api/exchange-rates/latest?sourceCurrency={sourceCurrency}&targetCurrency={targetCurrency}
- Persist exchange rates in MySQL.
- Retrieve the latest exchange rate by exact source/target pair.
- Use validAt descending and createdAt descending as ordering criteria.
- Return structured error when latest exchange rate is not found.
- Return structured validation errors for invalid request fields.
- Keep exchange-rate direction explicit.
- Do not silently invert exchange rates.

Supported currencies:
- BRL
- USD

Exchange-rate rules:
- sourceCurrency is required.
- targetCurrency is required.
- sourceCurrency must be supported.
- targetCurrency must be supported.
- sourceCurrency and targetCurrency must be different.
- rate is required.
- rate must be greater than zero.
- validAt is required.
- direction is explicit.
- USD -> BRL is not the same as BRL -> USD.
- If only USD -> BRL exists, BRL -> USD lookup must fail.

API contract requirements:
Follow docs/specs/04-api-contract.md.

Required endpoints:
- GET /api/reference-data/currencies
- GET /api/reference-data/receivable-types
- POST /api/exchange-rates
- GET /api/exchange-rates/latest

Request for POST /api/exchange-rates:
{
  "sourceCurrency": "USD",
  "targetCurrency": "BRL",
  "rate": 5.25000000,
  "validAt": "2026-07-07T13:00:00Z"
}

Expected response for POST /api/exchange-rates:
- 201 Created
- Location header
- id
- sourceCurrency
- targetCurrency
- rate
- validAt
- createdAt

Expected response for GET /api/exchange-rates/latest:
- 200 OK when an exact pair exists
- 404 Not Found when no exact pair exists

Architecture requirements:
- Controller must be thin.
- Controller must not contain business rules.
- Application services must orchestrate use cases.
- Infrastructure repositories must handle persistence.
- Domain/value objects may enforce currency and exchange-rate invariants.
- Do not place SQL or JPA details in controllers.
- Keep implementation aligned with docs/specs/06-architecture.md.

Expected components:
- ReferenceDataController
- ExchangeRateController
- CreateExchangeRateRequest
- ExchangeRateResponse
- CurrencyResponse
- ReceivableTypeResponse, if implementing receivable type reference data
- CreateExchangeRateService
- GetLatestExchangeRateService
- ListReferenceDataService
- ExchangeRate
- CurrencyCode
- Rate
- ExchangeRateRepository or equivalent abstraction
- JpaExchangeRateRepository or Spring Data repository
- CurrencyRepository or reference-data repository
- ExchangeRateNotFoundException or equivalent business exception
- InvalidCurrencyPairException or equivalent business exception
- UnsupportedCurrencyException or equivalent business exception

Persistence requirements:
- Use existing schema from docs/specs/05-data-model.md.
- exchange_rates must include:
  - id
  - source_currency_code
  - target_currency_code
  - rate
  - valid_at
  - created_at
- currencies must include:
  - code
  - name
  - decimal_places
  - created_at
  - updated_at
- rate must use DECIMAL in database.
- Java representation of rate must use BigDecimal.
- Do not use double, float, Double or Float for exchange rates.

Financial safety constraints:
- Use BigDecimal for rate values.
- Do not construct BigDecimal from floating-point literals.
- Do not hardcode exchange rates in business logic.
- Do not silently invert rates.
- Do not introduce pricing calculations in this task.
- Do not introduce settlement calculations in this task.

Validation requirements:
- Reject unsupported source currency.
- Reject unsupported target currency.
- Reject same source and target currency.
- Reject null or blank source currency.
- Reject null or blank target currency.
- Reject null rate.
- Reject zero rate.
- Reject negative rate.
- Reject null validAt.
- Return structured API errors.

Testing requirements:
Add or update tests for:
- currency reference-data endpoint returns BRL and USD.
- receivable type reference-data endpoint returns supported types, if implemented here.
- create exchange rate with valid request.
- reject exchange rate with same source and target currency.
- reject exchange rate with unsupported source currency.
- reject exchange rate with unsupported target currency.
- reject exchange rate with zero rate.
- reject exchange rate with negative rate.
- reject exchange rate with missing validAt.
- retrieve latest exchange rate by exact pair.
- retrieve latest exchange rate using validAt descending.
- deterministic tie-breaking using createdAt descending, if applicable.
- do not return inverse pair.
- return 404 when latest exchange rate does not exist.
- response uses structured error format.
- no floating-point types are used for exchange-rate value.

Expected commands:
- cd backend
- mvn test

Constraints:
- Do not implement pricing engine in this task.
- Do not implement settlement flow in this task.
- Do not implement statement query in this task.
- Do not implement frontend in this task.
- Do not add a real external exchange-rate provider.
- Do not add scheduled exchange-rate synchronization.
- Do not introduce unnecessary dependencies.
- Do not use H2 as the primary persistence behavior for this feature.
- Do not change API contracts without reporting the mismatch first.

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
- Confirm exact exchange-rate direction.
- Confirm latest-rate lookup ordering.
- Confirm BigDecimal usage.
- Confirm no double/float exchange-rate code.
- Confirm validation behavior.
- Confirm controller remains thin.
- Confirm no pricing/settlement behavior was implemented.

Risks or follow-ups:
- ...
```

## Expected AI Output

The AI agent should implement a focused currency engine feature.

Expected backend additions may include files similar to:

```text id="pqum2x"
backend/src/main/java/com/srm/creditengine/api/controller/ReferenceDataController.java
backend/src/main/java/com/srm/creditengine/api/controller/ExchangeRateController.java
backend/src/main/java/com/srm/creditengine/api/request/CreateExchangeRateRequest.java
backend/src/main/java/com/srm/creditengine/api/response/ExchangeRateResponse.java
backend/src/main/java/com/srm/creditengine/api/response/CurrencyResponse.java
backend/src/main/java/com/srm/creditengine/api/response/ReceivableTypeResponse.java
backend/src/main/java/com/srm/creditengine/application/exchange/CreateExchangeRateService.java
backend/src/main/java/com/srm/creditengine/application/exchange/GetLatestExchangeRateService.java
backend/src/main/java/com/srm/creditengine/application/reference/ListReferenceDataService.java
backend/src/main/java/com/srm/creditengine/domain/currency/CurrencyCode.java
backend/src/main/java/com/srm/creditengine/domain/exchange/ExchangeRate.java
backend/src/main/java/com/srm/creditengine/domain/shared/Rate.java
backend/src/main/java/com/srm/creditengine/infrastructure/persistence/ExchangeRateEntity.java
backend/src/main/java/com/srm/creditengine/infrastructure/repository/ExchangeRateJpaRepository.java
backend/src/main/java/com/srm/creditengine/infrastructure/repository/CurrencyJpaRepository.java
```

Exact names may vary if they remain aligned with the architecture and API contract.

## Required API Contract

## 1. List Supported Currencies

```http id="0isadc"
GET /api/reference-data/currencies
```

Expected response:

```json id="ay018a"
{
  "currencies": [
    {
      "code": "BRL",
      "name": "Brazilian Real",
      "decimalPlaces": 2
    },
    {
      "code": "USD",
      "name": "US Dollar",
      "decimalPlaces": 2
    }
  ]
}
```

## 2. List Receivable Types

```http id="txrzll"
GET /api/reference-data/receivable-types
```

Expected response:

```json id="flj2m9"
{
  "receivableTypes": [
    {
      "code": "MERCANTILE_DUPLICATE",
      "description": "Mercantile Duplicate",
      "monthlySpread": 0.01500000
    },
    {
      "code": "POST_DATED_CHECK",
      "description": "Post-Dated Check",
      "monthlySpread": 0.02500000
    }
  ]
}
```

## 3. Create Exchange Rate

```http id="t9ikzy"
POST /api/exchange-rates
```

Request:

```json id="l9kj9o"
{
  "sourceCurrency": "USD",
  "targetCurrency": "BRL",
  "rate": 5.25000000,
  "validAt": "2026-07-07T13:00:00Z"
}
```

Response:

```json id="c43vri"
{
  "id": "9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01",
  "sourceCurrency": "USD",
  "targetCurrency": "BRL",
  "rate": 5.25000000,
  "validAt": "2026-07-07T13:00:00Z",
  "createdAt": "2026-07-07T13:05:00Z"
}
```

Expected status:

```http id="nyoj5d"
201 Created
```

Expected header:

```http id="4yi6bm"
Location: /api/exchange-rates/{id}
```

## 4. Get Latest Exchange Rate

```http id="7zoxw2"
GET /api/exchange-rates/latest?sourceCurrency=USD&targetCurrency=BRL
```

Response:

```json id="90a5oq"
{
  "id": "9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01",
  "sourceCurrency": "USD",
  "targetCurrency": "BRL",
  "rate": 5.25000000,
  "validAt": "2026-07-07T13:00:00Z",
  "createdAt": "2026-07-07T13:05:00Z"
}
```

Expected status:

```http id="sfeau9"
200 OK
```

If not found:

```http id="11d3lr"
404 Not Found
```

## Business Rules to Preserve

## 1. Exchange Rate Direction Is Explicit

Correct:

```text id="g4xtqg"
sourceCurrency = USD
targetCurrency = BRL
```

is different from:

```text id="8u20ga"
sourceCurrency = BRL
targetCurrency = USD
```

Do not implement automatic inversion.

## 2. Latest Rate Selection

Latest exchange rate must be selected by:

```text id="fzyl15"
1. sourceCurrency
2. targetCurrency
3. validAt descending
4. createdAt descending
```

## 3. Positive Rate

Exchange rate must be:

```text id="cnp3ih"
rate > 0
```

Invalid values:

```text id="tw8lag"
0
-1
null
```

## 4. Same Currency Is Invalid for Exchange-Rate Registration

Invalid:

```text id="cyv72c"
sourceCurrency = BRL
targetCurrency = BRL
```

Same-currency pricing does not require an exchange rate, but exchange-rate registration must use different currencies.

## 5. Supported Currencies Only

Valid:

```text id="0w4dcg"
BRL
USD
```

Invalid:

```text id="6rj3fo"
EUR
REAL
brl
""
null
```

## Required Tests

## 1. Reference Data Tests

Required cases:

```text id="xpnypg"
shouldListSupportedCurrencies
shouldListReceivableTypesWithMonthlySpreads
```

## 2. Exchange Rate Creation Tests

Required cases:

```text id="y9yl9c"
shouldCreateExchangeRate
shouldRejectExchangeRateWithSameSourceAndTarget
shouldRejectExchangeRateWithUnsupportedSourceCurrency
shouldRejectExchangeRateWithUnsupportedTargetCurrency
shouldRejectExchangeRateWithZeroRate
shouldRejectExchangeRateWithNegativeRate
shouldRejectExchangeRateWithoutValidAt
```

## 3. Exchange Rate Lookup Tests

Required cases:

```text id="b9zmtf"
shouldFindLatestExchangeRateByExactPair
shouldReturnMostRecentValidExchangeRate
shouldUseCreatedAtAsTieBreakerWhenValidAtIsEqual
shouldNotFindInverseExchangeRateWhenOnlyOppositePairExists
shouldReturnNotFoundWhenLatestExchangeRateDoesNotExist
```

## 4. API Error Tests

Required cases:

```text id="2dbkgg"
shouldReturnStructuredErrorForInvalidCurrencyPair
shouldReturnStructuredErrorForMissingExchangeRate
shouldReturnStructuredValidationErrorForInvalidRate
```

## Acceptance Criteria

This task is acceptable when:

```text id="v486tg"
[ ] GET /api/reference-data/currencies exists.
[ ] GET /api/reference-data/receivable-types exists or already exists.
[ ] POST /api/exchange-rates exists.
[ ] GET /api/exchange-rates/latest exists.
[ ] Exchange rates are persisted.
[ ] Latest exchange rate lookup uses exact direction.
[ ] Inverse exchange rate is not inferred.
[ ] Rate uses BigDecimal in Java.
[ ] Rate uses DECIMAL in database.
[ ] No double/float is used for exchange-rate value.
[ ] Same source and target currency is rejected.
[ ] Unsupported currencies are rejected.
[ ] Zero or negative rate is rejected.
[ ] Missing latest exchange rate returns 404.
[ ] Validation errors are structured.
[ ] Business errors are structured.
[ ] Controllers remain thin.
[ ] Tests cover creation, validation and lookup.
[ ] mvn test passes.
[ ] No pricing, settlement or frontend behavior was implemented.
```

## Review Checklist for Author

After Codex completes the task, verify:

```text id="a6utk7"
[ ] Did it avoid changing unrelated files?
[ ] Did it implement only currency/exchange-rate behavior?
[ ] Did it avoid pricing or settlement implementation?
[ ] Did it keep controllers thin?
[ ] Did it use BigDecimal for rate?
[ ] Did it avoid double/float?
[ ] Did it avoid new BigDecimal(double)?
[ ] Did it validate supported currencies?
[ ] Did it reject same source and target currencies?
[ ] Did it retrieve latest rate by exact pair?
[ ] Did it avoid silent exchange-rate inversion?
[ ] Did it return 404 for missing latest rate?
[ ] Did it return structured errors?
[ ] Did it add meaningful tests?
[ ] Did mvn test pass?
[ ] Does AI_USAGE.md need an update?
```

## Common AI Mistakes to Reject

Reject or correct the output if the AI:

```text id="vtj6ud"
- silently inverts exchange rates;
- stores rates as double or float;
- uses new BigDecimal(5.25);
- hardcodes exchange rates in services;
- treats BRL -> USD and USD -> BRL as interchangeable;
- allows sourceCurrency and targetCurrency to be equal;
- skips supported-currency validation;
- returns latest exchange rate without deterministic ordering;
- implements pricing behavior in this task;
- implements settlement behavior in this task;
- changes frontend code;
- adds a real external FX provider;
- adds scheduled jobs;
- adds unnecessary dependencies;
- exposes stack traces in API responses;
- bypasses structured error handling.
```

## Suggested PR Description

````md id="685u47"
## Summary

Implements currency reference data and exchange-rate management.

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

- Added supported currency reference-data endpoint.
- Added receivable type reference-data endpoint.
- Added exchange-rate creation endpoint.
- Added latest exchange-rate lookup endpoint.
- Added exchange-rate validation.
- Added exact-pair direction handling.
- Added tests for exchange-rate creation and lookup.

## Tests

- [x] Unit tests added/updated
- [x] Integration/API tests added/updated, if available
- [x] Manual validation performed

Commands executed:

```bash
cd backend
mvn test
````

## Risks and Trade-offs

* Exchange rates are manually registered in the initial version.
* No external FX provider integration is included.
* Exchange-rate inversion is intentionally not automatic.

## AI Usage

AI was used for:

* implementation support
* test generation support

AI_USAGE.md:

* [ ] Updated
* [ ] Not applicable

````id="m907ep"

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
