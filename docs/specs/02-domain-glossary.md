# 02 - Domain Glossary

## 1. Purpose

This document defines the main business, financial and technical terms used in the **SRM Credit Engine** project.

The glossary exists to keep naming consistent across:

* source code;
* API contracts;
* database schema;
* tests;
* documentation;
* prompts used for AI-assisted development.

When implementing the system, prefer the terms defined here instead of creating alternative names for the same concept.

## 2. Naming Principles

The project uses English names in code, APIs, database objects and documentation files.

Domain terms originally expressed in Portuguese may appear in this glossary with their English equivalent.

Examples:

| Portuguese          | English used in project |
| ------------------- | ----------------------- |
| Cedente             | Assignor                |
| Recebivel           | Receivable              |
| Duplicata Mercantil | Mercantile Duplicate    |
| Cheque Pre-datado   | Post-Dated Check        |
| Valor de Face       | Face Value              |
| Valor Presente      | Present Value           |
| Desagio             | Discount                |
| Liquidacao          | Settlement              |
| Cambio              | Exchange Rate           |
| Moeda de Pagamento  | Payment Currency        |

## 3. Business Actors

### 3.1 Assignor

A company that sells or assigns receivables to the fund in exchange for liquidity.

In the project, the assignor is the business entity associated with receivables and settlements.

Recommended code names:

```text
Assignor
AssignorEntity
AssignorRepository
assignorId
```

Recommended database name:

```text
assignors
```

---

### 3.2 Operator

Internal user from the operations desk responsible for simulating and registering receivable settlements.

Authentication and user management are outside the initial scope, so the operator is treated as a conceptual actor rather than a persisted user entity in the first version.

Recommended usage:

```text
operator frontend
operator panel
pricing simulation screen
settlement statement screen
```

---

### 3.3 Backoffice Analyst

Internal user responsible for reviewing historical settlements, checking audit information and filtering settlement statements.

This actor is also conceptual in the initial version.

Recommended usage:

```text
settlement statement
settlement history
audit review
```

---

### 3.4 Technical Evaluator

Reviewer of the technical challenge.

The system documentation must help this actor understand:

* how to run the project;
* which decisions were made;
* how the domain was modeled;
* how financial precision was handled;
* how Git and AI were used.

## 4. Core Financial Terms

### 4.1 Receivable

A credit asset that can be priced and settled.

A receivable represents an amount expected to be paid in the future.

Examples in this project:

* Mercantile Duplicate;
* Post-Dated Check.

Recommended code names:

```text
Receivable
ReceivableEntity
ReceivableRepository
receivableId
```

Recommended database name:

```text
receivables
```

---

### 4.2 Receivable Type

The classification of a receivable according to its financial nature and risk rule.

Initial supported values:

```text
MERCANTILE_DUPLICATE
POST_DATED_CHECK
```

The receivable type determines which spread strategy must be applied during pricing.

Recommended code names:

```text
ReceivableType
ReceivableTypeCode
```

Recommended database name:

```text
receivable_types
```

---

### 4.3 Mercantile Duplicate

A receivable type representing a commercial invoice-like credit instrument.

In this project, it has an initial monthly spread of:

```text
1.5% a.m. = 0.015
```

Recommended enum value:

```text
MERCANTILE_DUPLICATE
```

Recommended strategy name:

```text
MercantileDuplicatePricingStrategy
```

---

### 4.4 Post-Dated Check

A receivable type representing a check with a future payment date.

In this project, it has an initial monthly spread of:

```text
2.5% a.m. = 0.025
```

Recommended enum value:

```text
POST_DATED_CHECK
```

Recommended strategy name:

```text
PostDatedCheckPricingStrategy
```

---

### 4.5 Face Value

The original nominal value of a receivable before discounting.

This is the amount expected at the receivable due date.

Recommended code names:

```text
faceValue
FaceValue
```

Recommended database column:

```text
face_value
```

Rules:

* must be greater than zero;
* must use decimal-safe representation;
* must not be represented with floating-point types.

---

### 4.6 Present Value

The discounted value of a receivable calculated from:

* face value;
* base rate;
* spread;
* term.

Formula:

```text
Present Value = Face Value / (1 + Base Rate + Spread) ^ Term
```

Recommended code names:

```text
presentValue
PresentValue
```

Recommended database columns:

```text
present_value_source
total_present_value
```

Rules:

* must be calculated in the source currency first;
* must use decimal-safe representation;
* must be persisted as part of the settlement audit snapshot.

---

### 4.7 Discount

The economic difference between the face value and the present value of a receivable.

In Portuguese, this is the project equivalent of **desagio**.

Formula:

```text
Discount = Face Value - Present Value
```

Recommended code names:

```text
discount
discountValue
```

Recommended database column:

```text
discount_value
```

Rules:

* must be derived from the calculation snapshot;
* must be auditable;
* must use decimal-safe representation.

---

### 4.8 Net Payment Value

The final amount to be paid for a receivable or settlement item after discounting and, when applicable, currency conversion.

Recommended code names:

```text
netPaymentValue
paymentValue
```

Recommended database columns:

```text
payment_value
total_payment_value
```

Rules:

* in same-currency operations, it is equivalent to present value after monetary rounding;
* in cross-currency operations, it is the present value converted to payment currency;
* must be persisted in the settlement item.

---

### 4.9 Base Rate

The base financial rate used in the pricing formula.

In this project, the base rate is treated as a monthly rate.

Recommended code names:

```text
baseRate
BaseRate
```

Recommended database column:

```text
base_rate
```

Rules:

* must be greater than or equal to zero;
* must use decimal representation;
* request-provided base rate takes precedence over any configured server-side fallback;
* if omitted from the request, it may be resolved from `DEFAULT_BASE_RATE` when the implementation enables that fallback;
* must be persisted in each settlement item snapshot;
* must not be hardcoded in pricing logic without explicit documentation.

---

### 4.10 Spread

The risk premium applied according to the receivable type.

Initial monthly spreads:

```text
MERCANTILE_DUPLICATE = 0.015
POST_DATED_CHECK    = 0.025
```

Recommended code names:

```text
spread
RiskSpread
```

Recommended database column:

```text
spread
```

Rules:

* must be selected through Strategy Pattern or equivalent domain abstraction;
* must use decimal representation;
* must be persisted in each settlement item snapshot;
* must not be calculated in controllers or frontend components.

---

### 4.11 Term

The time factor used as the exponent in the present value formula.

Recommended code names:

```text
term
termInMonths
```

Recommended database column:

```text
term_in_months
```

Initial project assumption:

```text
termInMonths = days between pricing date and due date / 30
```

Rules:

* must be greater than zero for a valid receivable;
* past-due receivables must be rejected;
* same-day due dates must be rejected in the initial version;
* must be persisted in each settlement item snapshot.

---

### 4.12 Due Date

The future date when the receivable is expected to be paid by the original debtor.

Recommended code name:

```text
dueDate
```

Recommended database column:

```text
due_date
```

Rules:

* must be present;
* must be a future date;
* determines the term used in pricing.

---

### 4.13 Pricing Date

The date used as the starting point for term calculation.

Initial project assumption:

```text
pricingDate = current application date at simulation or settlement time
```

Recommended code name:

```text
pricingDate
```

Rules:

* must be consistently used during calculation;
* should be included in internal calculation results;
* may be persisted directly or indirectly through calculation timestamp.

---

### 4.14 Calculation Timestamp

The exact date and time when a pricing or settlement calculation was performed.

Recommended code name:

```text
calculatedAt
```

Recommended database column:

```text
calculated_at
```

Rules:

* must be persisted for settlement items;
* helps explain historical calculations;
* should use UTC in persistence.

## 5. Currency and Exchange Terms

### 5.1 Currency

A monetary unit supported by the platform.

Initial supported currencies:

```text
BRL
USD
```

Recommended code names:

```text
Currency
CurrencyCode
```

Recommended database name:

```text
currencies
```

Rules:

* currency codes must follow ISO-like three-letter uppercase format;
* unsupported currencies must be rejected;
* monetary values must always be associated with a currency.

---

### 5.2 Source Currency

The currency in which the receivable is originally denominated.

Recommended code name:

```text
sourceCurrency
```

Recommended database column:

```text
currency_code
```

For settlement and exchange-rate structures, use:

```text
source_currency_code
```

Example:

```text
A receivable with face value of BRL 10,000 has sourceCurrency = BRL.
```

---

### 5.3 Payment Currency

The currency used to pay the settlement.

Recommended code name:

```text
paymentCurrency
```

Recommended database column:

```text
payment_currency_code
```

Example:

```text
A BRL receivable settled in USD has paymentCurrency = USD.
```

---

### 5.4 Same-Currency Operation

A pricing or settlement operation where the source currency and payment currency are the same.

Example:

```text
sourceCurrency = BRL
paymentCurrency = BRL
```

Rules:

* no exchange-rate conversion is required;
* the net payment value is derived from the present value in the same currency.

---

### 5.5 Cross-Currency Operation

A pricing or settlement operation where the source currency and payment currency are different.

Example:

```text
sourceCurrency = BRL
paymentCurrency = USD
```

Rules:

* an exchange rate must be available;
* present value must be calculated in the source currency first;
* currency conversion must be applied after present value calculation;
* the exchange rate used must be persisted as a snapshot in settlements.

---

### 5.6 Exchange Rate

A rate used to convert an amount from one currency to another.

Recommended code names:

```text
ExchangeRate
exchangeRate
```

Recommended database name:

```text
exchange_rates
```

Recommended columns:

```text
source_currency_code
target_currency_code
rate
valid_at
created_at
```

Rules:

* must be greater than zero;
* must have a source currency;
* must have a target currency;
* must have a validity timestamp;
* must be retrievable as the latest rate for a currency pair;
* must not be hardcoded in pricing logic.

---

### 5.7 Exchange Rate Pair

A pair composed of source currency and target currency.

Example:

```text
USD -> BRL
BRL -> USD
```

Recommended code name:

```text
ExchangeRatePair
```

Rules:

* source and target currencies must be different for a conversion pair;
* each direction is explicit;
* `USD -> BRL` is not the same stored pair as `BRL -> USD`.

---

### 5.8 Exchange Rate Snapshot

The exchange rate value persisted in a settlement item at the time of settlement.

Recommended code name:

```text
exchangeRateSnapshot
```

Recommended database column:

```text
exchange_rate
```

Rules:

* must preserve the historical rate used in the calculation;
* must not change when new rates are registered;
* may be null for same-currency operations;
* is required for cross-currency settlement items.

## 6. Settlement Terms

### 6.1 Settlement

An auditable transaction representing the acquisition or liquidation of one or more receivables.

Recommended code names:

```text
Settlement
SettlementEntity
SettlementRepository
```

Recommended database name:

```text
settlements
```

Rules:

* must be associated with an assignor;
* must have one common source currency at header level in the initial batch design;
* must have a payment currency;
* must have one or more settlement items;
* must be persisted atomically;
* must not be partially completed.

---

### 6.2 Settlement Item

An individual receivable calculation within a settlement batch.

Recommended code names:

```text
SettlementItem
SettlementItemEntity
```

Recommended database name:

```text
settlement_items
```

Rules:

* must belong to one settlement;
* must reference one receivable;
* must persist the calculation snapshot;
* must prevent duplicate settlement of the same receivable.

---

### 6.3 Settlement Batch

A group of receivables submitted together for settlement.

Recommended code name:

```text
SettlementBatch
```

Rules:

* must contain at least one receivable;
* all receivables in the same batch must share one source currency in the initial version;
* must be validated before persistence;
* must be processed atomically;
* must rollback completely if any item is invalid.

---

### 6.4 Settlement Statement

An analytical view of historical settlements.

Recommended code names:

```text
SettlementStatement
SettlementStatementQuery
SettlementStatementRow
```

Recommended API path:

```text
GET /api/settlements/statement
```

Rules:

* must support filters;
* must support server-side pagination;
* must not load the full dataset into memory before filtering;
* may use optimized SQL or projections.

---

### 6.5 Settlement Status

The state of a settlement.

Initial recommended values:

```text
PENDING
SETTLED
FAILED
CANCELLED
```

Recommended code name:

```text
SettlementStatus
```

Recommended database column:

```text
status
```

Initial implementation expectation:

* successfully created settlements should usually be stored as `SETTLED`;
* failed attempts should not create partial settlement records;
* additional statuses may be used if the implementation introduces a more detailed workflow.

---

### 6.6 Receivable Status

The state of a receivable in relation to settlement.

Initial recommended values:

```text
AVAILABLE
SETTLED
CANCELLED
```

Recommended code name:

```text
ReceivableStatus
```

Recommended database column:

```text
status
```

Rules:

* only available receivables may be settled;
* a settled receivable must not be settled again.

---

### 6.7 Duplicate Settlement

An invalid situation where the same receivable is settled more than once.

Rules:

* must be prevented by business logic;
* should also be prevented by database constraints;
* should return a conflict response when detected through API.

Recommended HTTP status:

```text
409 Conflict
```

## 7. Pricing Engine Terms

### 7.1 Pricing Engine

The domain component responsible for calculating receivable pricing.

Recommended code names:

```text
PricingEngine
PricingService
```

Responsibilities:

* receive validated pricing inputs;
* resolve the appropriate pricing strategy;
* calculate present value;
* calculate discount;
* apply exchange conversion when needed;
* return calculation results.

---

### 7.2 Pricing Strategy

A Strategy Pattern implementation responsible for the risk rule associated with a receivable type.

Recommended code name:

```text
PricingStrategy
```

Recommended implementations:

```text
MercantileDuplicatePricingStrategy
PostDatedCheckPricingStrategy
```

Rules:

* each supported receivable type must have a pricing strategy;
* strategies must not perform persistence;
* strategies must be unit tested;
* the application must not use large conditional blocks as a replacement for strategy resolution.

---

### 7.3 Pricing Strategy Resolver

A component responsible for selecting the correct strategy for a receivable type.

Recommended code name:

```text
PricingStrategyResolver
```

Rules:

* must return the correct strategy for each supported receivable type;
* must fail clearly when an unsupported type is requested;
* must be covered by tests.

---

### 7.4 Pricing Simulation

A non-persistent calculation requested by the operator to preview pricing results.

Recommended code names:

```text
PricingSimulation
PricingSimulationRequest
PricingSimulationResponse
```

Recommended API path:

```text
POST /api/pricing/simulations
```

Rules:

* must not create settlement records;
* must return calculation details;
* must use the same domain pricing logic as settlement;
* must be calculated by backend.

---

### 7.5 Pricing Result

The calculated output of a pricing operation.

Recommended code name:

```text
PricingResult
```

Expected fields:

```text
faceValue
sourceCurrency
paymentCurrency
presentValueInSourceCurrency
netPaymentValue
discountValue
baseRate
spread
termInMonths
exchangeRate
calculatedAt
```

Rules:

* must be deterministic for the same inputs and same exchange rate;
* must expose enough data for auditability;
* must not hide calculation inputs.

## 8. Technical Architecture Terms

### 8.1 API Layer

The backend layer responsible for HTTP communication.

Includes:

* controllers;
* request DTOs;
* response DTOs;
* validation annotations;
* OpenAPI annotations;
* exception response mapping.

Must not include:

* financial calculation logic;
* transaction orchestration;
* database-specific implementation details.

---

### 8.2 Application Layer

The backend layer responsible for use case orchestration.

Includes:

* transactional services;
* settlement use cases;
* coordination between domain and infrastructure;
* application-level validation.

Must not include:

* HTTP-specific code;
* UI logic;
* low-level SQL details unless explicitly needed for a use case boundary.

---

### 8.3 Domain Layer

The backend layer responsible for business concepts and rules.

Includes:

* domain entities;
* value objects;
* pricing strategies;
* domain services;
* domain exceptions.

Must not depend on:

* controllers;
* frameworks;
* JPA repositories;
* HTTP DTOs.

---

### 8.4 Infrastructure Layer

The backend layer responsible for technical details.

Includes:

* database repositories;
* JPA entities;
* Flyway migrations;
* SQL queries;
* external or mocked integrations.

---

### 8.5 Value Object

A domain object identified by its value rather than identity.

Recommended examples:

```text
Money
Rate
CurrencyCode
Term
```

Rules:

* should validate its own invariants;
* should be immutable where practical;
* should help avoid primitive obsession in financial logic.

---

### 8.6 Entity

A domain object with identity.

Recommended examples:

```text
Assignor
Receivable
Settlement
SettlementItem
ExchangeRate
```

Rules:

* identity matters;
* lifecycle may be persisted;
* invariants must be protected.

---

### 8.7 Repository

A component that abstracts persistence access.

Recommended code names:

```text
ReceivableRepository
SettlementRepository
ExchangeRateRepository
```

Rules:

* repositories belong to the persistence boundary;
* domain logic must not be hidden in repository methods;
* analytical queries may use dedicated read repositories.

---

### 8.8 Application Service

A service responsible for executing a use case.

Recommended examples:

```text
CreateExchangeRateService
PricingSimulationService
CreateSettlementService
SettlementStatementService
```

Rules:

* orchestrates domain logic;
* defines transaction boundaries when needed;
* should be easier to test than controllers.

---

### 8.9 Domain Service

A service that contains domain behavior that does not naturally belong to a single entity or value object.

Recommended examples:

```text
PricingEngine
CurrencyConversionService
```

Rules:

* must not depend on HTTP;
* must not depend directly on frontend concepts;
* should be unit testable.

## 9. API and Error Terms

### 9.1 Request DTO

Object representing an incoming API request payload.

Recommended suffix:

```text
Request
```

Example:

```text
PricingSimulationRequest
```

Rules:

* may contain validation annotations;
* must not contain business behavior;
* must not be reused as a domain object.

---

### 9.2 Response DTO

Object representing an outgoing API response payload.

Recommended suffix:

```text
Response
```

Example:

```text
PricingSimulationResponse
```

Rules:

* should expose API contract fields;
* should not leak persistence implementation details.

---

### 9.3 Structured Error Response

Standard API response used when an error occurs.

Recommended code name:

```text
ApiErrorResponse
```

Expected fields:

```text
timestamp
status
error
message
path
details
```

Rules:

* validation errors should include field-level details;
* internal stack traces must not be exposed;
* error responses should be consistent across endpoints.

---

### 9.4 Validation Error

An error caused by invalid input.

Examples:

* missing required field;
* negative face value;
* invalid currency;
* invalid date range;
* invalid page size.

Recommended HTTP status:

```text
400 Bad Request
```

---

### 9.5 Business Rule Violation

An error caused by valid input that violates a domain rule.

Examples:

* missing exchange rate for cross-currency operation;
* past-due receivable;
* unsupported receivable type;
* attempted duplicate settlement.

Recommended HTTP statuses:

```text
409 Conflict
422 Unprocessable Entity
```

## 10. Database Terms

### 10.1 Migration

A versioned database change managed by Flyway.

Recommended location:

```text
backend/src/main/resources/db/migration
```

Recommended naming:

```text
V1__create_initial_schema.sql
V2__seed_reference_data.sql
V3__add_settlement_indexes.sql
```

Rules:

* schema changes must be versioned;
* final project must not rely only on ORM auto-DDL;
* migrations must be deterministic.

---

### 10.2 DDL

Data Definition Language used to define database structure.

Examples:

```text
CREATE TABLE
ALTER TABLE
CREATE INDEX
```

The project DDL is represented through Flyway migration scripts.

---

### 10.3 Constraint

A database rule that protects data integrity.

Examples:

* primary key;
* foreign key;
* unique key;
* check constraint.

Expected use cases:

* prevent invalid relationships;
* prevent duplicate settlement items for the same receivable;
* enforce positive monetary values where supported;
* support referential integrity.

---

### 10.4 Index

A database structure used to improve query performance.

Expected indexes:

* settlement date;
* assignor;
* payment currency;
* receivable type;
* exchange-rate pair and validity date.

Indexes must support the settlement statement query requirements.

## 11. Frontend Terms

### 11.1 Operator Panel

The Angular frontend interface used by operators.

Includes:

* pricing simulation screen;
* settlement history screen;
* exchange-rate interaction if implemented.

---

### 11.2 Pricing Simulation Screen

Frontend screen where the operator inputs receivable data and receives calculated pricing results from the backend.

Rules:

* must call backend simulation endpoint;
* must display validation errors clearly;
* must not be the official calculation source.

---

### 11.3 Settlement Grid

Frontend table that displays settlement history.

Rules:

* must use server-side pagination;
* must support filters;
* must not load the entire settlement history into the browser.

---

### 11.4 Server-Side Pagination

Pagination performed by the backend and database, not by the browser after loading all records.

Expected request parameters:

```text
page
size
sort
```

Expected response metadata:

```text
page
size
totalElements
totalPages
```

---

### 11.5 Reactive Form

Angular form model used for operator inputs.

Expected use cases:

* pricing simulation form;
* exchange-rate form;
* settlement statement filters.

---

### 11.6 Signal

Angular reactive primitive used for component or service state.

Expected use cases:

* loading state;
* selected filters;
* simulation result;
* error state.

## 12. Git and Delivery Terms

### 12.1 Main Branch

Stable branch used for final delivery.

Recommended name:

```text
main
```

Rules:

* should contain final stable version;
* final release tag is created from this branch.

---

### 12.2 Develop Branch

Integration branch used before final delivery.

Recommended name:

```text
develop
```

Rules:

* feature branches are created from `develop`;
* feature Pull Requests are merged back into `develop`;
* final `develop` is merged into `main`.

---

### 12.3 Feature Branch

Branch used for isolated implementation work.

Recommended pattern:

```text
feature/<feature-name>
```

Examples:

```text
feature/pricing-engine
feature/currency-engine
feature/settlement-flow
```

---

### 12.4 Simulated Pull Request

A Pull Request created even in a solo project to demonstrate work organization, review discipline and traceability.

Expected contents:

* summary;
* specs covered;
* tests added;
* risks;
* screenshots when frontend changes exist.

---

### 12.5 Conventional Commit

Commit message format used to keep history readable.

Examples:

```text
feat: add pricing strategy engine
fix: correct cross-currency rounding
test: cover settlement rollback
docs: update business rules
chore: configure docker compose
```

---

### 12.6 Release Tag

Git tag marking the final delivered version.

Expected final tag:

```text
v1.0.0
```

## 13. AI-Assisted Development Terms

### 13.1 AI Co-Pilot

An AI tool used to assist the author with planning, coding, tests, refactoring or documentation.

In this project, AI output is treated as a draft that requires review.

---

### 13.2 Codex Task

A small scoped implementation or review task executed with Codex.

Rules:

* must reference relevant specs;
* must reference relevant ADRs;
* must define allowed scope;
* must define forbidden changes;
* must request tests when needed;
* must not ask Codex to implement the entire project at once.

---

### 13.3 AI Usage Log

The record of meaningful AI usage kept in:

```text
AI_USAGE.md
```

Expected content:

* tool used;
* prompt summary;
* AI contribution;
* author review;
* accepted changes;
* rejected or corrected output;
* validation performed.

## 14. Preferred Code Naming Summary

### 14.1 Domain Classes

```text
Assignor
Receivable
ReceivableType
Settlement
SettlementItem
ExchangeRate
Money
Rate
CurrencyCode
Term
PricingResult
```

### 14.2 Strategies and Services

```text
PricingStrategy
MercantileDuplicatePricingStrategy
PostDatedCheckPricingStrategy
PricingStrategyResolver
PricingEngine
CurrencyConversionService
PricingSimulationService
CreateSettlementService
SettlementStatementService
```

### 14.3 API DTOs

```text
CreateExchangeRateRequest
ExchangeRateResponse
PricingSimulationRequest
PricingSimulationResponse
CreateSettlementRequest
SettlementResponse
SettlementStatementResponse
ApiErrorResponse
```

### 14.4 Repositories

```text
AssignorRepository
ReceivableRepository
ExchangeRateRepository
SettlementRepository
SettlementStatementRepository
```

### 14.5 Database Tables

```text
currencies
exchange_rates
assignors
receivable_types
receivables
settlements
settlement_items
```

## 15. Terms to Avoid

Avoid using inconsistent or vague names such as:

```text
Client
Customer
UserCompany
Operation
TransactionData
MoneyValue
CurrencyTax
Liquidation
Tax
Fee
```

Use project-defined terms instead:

```text
Assignor
Receivable
Settlement
ExchangeRate
BaseRate
Spread
Discount
PaymentValue
```

Exception:

The term `transaction` may be used when referring specifically to database transactions or ACID behavior. For business records, prefer `Settlement`.

## 16. Glossary Change Rule

If implementation introduces a new domain concept, this glossary must be updated.

Examples:

* new receivable type;
* new settlement status;
* new pricing rule;
* new audit concept;
* new integration boundary;
* new frontend module.

The glossary should remain aligned with:

* `03-business-rules.md`;
* `04-api-contract.md`;
* `05-data-model.md`;
* `06-architecture.md`;
* ADRs;
* database migrations;
* source code.
